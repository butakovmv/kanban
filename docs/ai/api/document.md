# API документов проекта

Базовый путь: `/api/v1/projects/{id}/documents`

## Типы документов

Система поддерживает 14 предопределённых типов документов (см. [Функциональные требования](../../FUNCTIONAL_REQUIRMENTS.md#3-управление-документами-проекта)).

## Эндпоинты

### `GET /api/v1/projects/{id}/documents`
Список документов проекта, сгруппированных по типу.

- **Доступ:** участник проекта (`document:read`)
- **Ответ:** `200 OK` — `{ "types": [ { "type": "string", "label": "string", "document?": { "id", "name", "updatedAt", "size" } } ] }`
- **Примечание:** тип документа задаётся enum-полем, каждый тип может иметь не более одного текущего документа

### `GET /api/v1/projects/{id}/documents/{docId}`
Содержимое документа.

- **Доступ:** участник проекта (`document:read`)
- **Параметры:** `?format=json|markdown` (по умолчанию `json` — возвращает метаданные + presigned URL для скачивания)
- **Ответ:** `200 OK` — `{ "id", "type", "name", "createdAt", "updatedAt", "presignedUrl": "https://..." }`

### `POST /api/v1/projects/{id}/documents`
Загрузка нового документа или создание через редактор.

- **Доступ:** участник проекта (`document:create`)
- **Вариант 1 (загрузка файла):** `multipart/form-data` — поля `type` (тип документа), `file` (содержимое, макс. 10 MB)
- **Вариант 2 (создание пустого):** `{ "type": "string", "name?": "string (1-200)" }`
- **Ответ:** `201 Created` — `{ "id": "uuid", "type", "name", "createdAt", "presignedUrl" }`
- **Ошибки:** `401`, `403`

### `PUT /api/v1/projects/{id}/documents/{docId}`
Обновление содержимого документа.

- **Доступ:** участник проекта (`document:update`)
- **Тело:** `multipart/form-data` — поле `file` (макс. 10 MB)
- **Ответ:** `200 OK`

### `DELETE /api/v1/projects/{id}/documents/{docId}`
Удаление документа.

- **Доступ:** участник проекта (`document:delete`)
- **Ответ:** `204 No Content`

## Связанные Use Case

- [UC-06-01](../../usecase/06-documents.md#uc-06-01) — просмотр списка документов
- [UC-06-02](../../usecase/06-documents.md#uc-06-02) — загрузка документа
