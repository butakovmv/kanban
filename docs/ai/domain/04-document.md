# Домен: Документы (Document)

## Обзор
Управление документами проекта. Каждый проект может содержать до 14 типов предопределённых документов (требования, сценарии, диаграммы, контракты, отчёты по тестированию и т.д.). Каждый тип — не более одного текущего документа.

**Границы:**
- Документ владеет только метаданными; содержимое хранится в MinIO
- Документы проекта — расширение проекта (см. `project-and-board`)

## Агрегаты

### `Document` (корень агрегата)
Документ проекта.

- **id:** `DocumentId` (value object)
- **projectId:** `ProjectId` (value object)
- **type:** `DocumentType` (value object, enum)
- **name:** String (1-200)
- **storageKey:** `StorageKey` (value object)
- **size:** Long (байты, макс. 10 MB)
- **createdAt:** Instant
- **updatedAt:** Instant

**Правила:**
- На один тип документа — не более одного текущего документа. Загрузка нового документа того же типа заменяет предыдущий (старый файл удаляется из MinIO)
- Тип документа задаётся enum (14 значений, см. FR раздел 3)

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `DocumentId` | `value: UUID` | Не null |
| `DocumentType` | `enum: CUSTOMER_REQUIREMENT, TECHNICAL_REQUIREMENT, UI_UX_REQUIREMENT, USER_STORY_LIST, ARCHITECTURE_DESCRIPTION, ADR_LIST, C4_DIAGRAMS, USE_CASE_DESCRIPTIONS, API_CONTRACTS, EXTERNAL_CONTRACTS, API_TESTS, UI_TESTS, ACCEPTANCE_TESTS, SECURITY_REPORT` | — |
| `StorageKey` | `value: String` | Формат: `projects/{projectId}/documents/{docId}/{filename}` |

## Операции

| Операция | Команда | Агрегат | Событие |
|---|---|---|---|
| Список документов | `ListDocuments(projectId)` | Document | — (query) |
| Получение документа | `GetDocument(documentId)` | Document | — (query) |
| Загрузка/создание | `UploadDocument(projectId, type, file)` | Document | `DocumentUploaded` |
| Обновление | `UpdateDocument(documentId, file)` | Document | `DocumentUpdated` |
| Удаление | `DeleteDocument(documentId)` | Document | `DocumentDeleted` |

## Связанные Use Case
- [UC-06-01](../usecase/06-documents.md#uc-06-01) — просмотр списка документов
- [UC-06-02](../usecase/06-documents.md#uc-06-02) — загрузка документа

## Связанные API
- `docs/ai/api/document.md` — 5 эндпоинтов
