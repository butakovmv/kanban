# Контракт: MinIO (File Storage)

## Обзор
S3-совместимое объектное хранилище для файлов проектов (вложения задач и документы). Входит в состав системы (см. ADR 260619-1200). Замена на внешний S3 (AWS S3, Yandex Object Storage) прозрачна — без смены API.

## Протокол
- **Протокол:** S3 API (REST-like) over HTTPS
- **Порт:** 9000 (API), 9090 (Web UI console)
- **Транспорт:** HTTP/1.1 (S3 API)
- **Аутентификация:** Access Key + Secret Key (AWS Signature V4)

## Поток вызова

```
┌──────────────┐   S3 API HTTPS :9000   ┌──────────────┐
│ API Container│ ─────────────────────→  │    MinIO     │
│  (WebFlux)  │  PutObject, GetObject,   │              │
│             │  DeleteObject,           │              │
│             │  PresignedGetObject,     │              │
│             │  PresignedPutObject      │              │
├──────────────┤                          ├──────────────┤
│   Web UI    │ ─────────────────────→  │              │
│  (браузер)  │  Presigned URL (PUT)     │              │
│             │  Presigned URL (GET)     │              │
└──────────────┘                          └──────────────┘
```

## Buckets

| Bucket | Назначение | Public Access |
|---|---|---|
| `kanban-files` | Вложения задач и документы проектов | Нет (presigned URL) |

## Операции API Container → MinIO

### Операции управления

| Операция | S3 API | Использование |
|---|---|---|
| Создание bucket | `PUT /kanban-files/` | При старте приложения (init) |
| Проверка существования bucket | `HEAD /kanban-files/` | Healthcheck |
| Загрузка файла (multipart upload) | `POST /kanban-files/{key}` | POST /tasks/{id}/files (сервер загружает в MinIO) |
| Удаление файла | `DELETE /kanban-files/{key}` | DELETE /tasks/{id}/files/{fileId}; замена документа |
| Генерация presigned GET URL | `PresignedGetObject(bucket, key, expiry)` | GET /tasks/{id}/files/{fileId}/presigned-url?action=download |
| Генерация presigned PUT URL | `PresignedPutObject(bucket, key, expiry)` | GET /tasks/{id}/files/{fileId}/presigned-url?action=upload |

### Presigned URL

**Параметры:**
- `expiry`: 3600 секунд (1 час)
- `method`: GET (download) или PUT (upload)
- `content-type`: задаётся при генерации для upload (защита от произвольного типа)

**Ответ клиенту:**

```json
{
  "url": "https://minio.example.com/kanban-files/projects/{projectId}/tasks/{taskId}/{filename}?...",
  "expiresIn": 3600
}
```

## Структура ключей (StorageKey)

### Вложения задач

```
projects/{projectId}/tasks/{taskId}/{uuid}-{filename}
```

Пример: `projects/a1b2c3d4/tasks/e5f6g7h8/a1b2c3d4-screenshot.png`

### Документы

```
projects/{projectId}/documents/{documentId}/{uuid}-{filename}
```

Пример: `projects/a1b2c3d4/documents/x9y8z7w6/abc123-requirements.pdf`

**Правила:**
- UUID в имени файла предотвращает коллизии
- Bucket + storage_key = уникальный идентификатор объекта в MinIO
- Не используются папки (S3 — плоское пространство), префикс / — соглашение

## Параметры соединения

| Параметр | Значение | Примечание |
|---|---|---|
| Endpoint | `http://minio:9000` (внутри docker-compose) | Внешний URL настраивается для production |
| Access Key | `minioadmin` (dev) / из secrets (prod) | `MINIO_ROOT_USER` |
| Secret Key | `minioadmin` (dev) / из secrets (prod) | `MINIO_ROOT_PASSWORD` |
| Region | `us-east-1` | S3 SDK требует; MinIO не проверяет |
| SSL | false (dev) / true (prod) | `useSSL` |
| Таймаут соединения | 10 секунд | `connectionTimeout` |
| Таймаут чтения | 30 секунд | `readTimeout` |
| Таймаут записи | 30 секунд | `writeTimeout` (для upload) |

## Обработка ошибок

| Ситуация | Код S3 | Действие сервера |
|---|---|---|
| Bucket не существует | `NoSuchBucket` | Создать bucket при старте (init) |
| Object не найден | `NoSuchKey` | `404 Not Found` для клиента |
| Access denied | `AccessDenied` | `403 Forbidden` (неверный key/secret) |
| Превышение лимита | `SlowDown` (MinIO rate limit) | Retry с backoff |
| Файл слишком большой (через API) | `EntityTooLarge` | `413 Payload Too Large` (10 MB limit) |
| Presigned URL истёк | `AccessDenied` | Клиент запрашивает новый presigned URL |
| MinIO недоступен | Connection refused | `503 Service Unavailable`, retry |

## Конфигурация (application.yml)

```yaml
kanban:
  storage:
    endpoint: "${MINIO_ENDPOINT:http://localhost:9000}"
    region: "us-east-1"
    bucket: "kanban-files"
    access-key: "${MINIO_ACCESS_KEY:minioadmin}"
    secret-key: "${MINIO_SECRET_KEY:minioadmin}"
    presigned-url-expiry: 3600
```

## Безопасность
- Bucket не публичный — доступ только через presigned URL
- Presigned URL живёт 1 час
- Upload presigned URL требует указания `content-type` (защита от загрузки .exe под видом .png)
- Все credentials — из переменных окружения, не в репозитории
- В production — HTTPS между API Container и MinIO

## Развёртывание (docker-compose)

```yaml
minio:
  image: minio/minio:latest
  command: server /data --console-address ":9090"
  ports:
    - "9000:9000"
    - "9090:9090"
  environment:
    MINIO_ROOT_USER: "${MINIO_ACCESS_KEY}"
    MINIO_ROOT_PASSWORD: "${MINIO_SECRET_KEY}"
  volumes:
    - minio-data:/data
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
    interval: 10s
```

## Замена на внешний S3 (production)

При переходе на AWS S3 или Yandex Object Storage меняется только конфигурация:

```yaml
kanban:
  storage:
    endpoint: "https://s3.yandexcloud.net"
    region: "ru-central1"
    bucket: "kanban-files-prod"
    access-key: "${S3_ACCESS_KEY}"
    secret-key: "${S3_SECRET_KEY}"
```

Никаких изменений в коде — S3 API идентичен.
