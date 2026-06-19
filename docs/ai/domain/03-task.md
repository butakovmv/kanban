# Домен: Задачи (Task)

## Обзор
Управление задачами проекта — создание, редактирование, перемещение между колонками/бэклогом/архивом, комментарии, прикреплённые файлы, метки, приоритеты, назначение исполнителя.

**Границы:**
- Задача владеет комментариями и файлами (в рамках своего агрегата)
- Не управляет колонками, свимлайнами, доступом (см. `project-and-board`, `access-control`)

## Агрегаты

### `Task` (корень агрегата)
Задача на канбан-доске.

- **id:** `TaskId` (value object)
- **projectId:** `ProjectId` (value object)
- **title:** String (1-500)
- **description:** String? (макс. 10000)
- **useCase:** String? (1-200)
- **priority:** `Priority` (value object)
- **status:** `TaskStatus` (value object)
- **labels:** `List<Label>` (value objects, 0-10 шт., каждый 1-50 символов)
- **assigneeId:** `UserId`? (может быть null)
- **workStartDate:** Instant? (фиксируется при первом перемещении из бэклога в колонку)
- **archivedAt:** Instant? (фиксируется при архивировании)
- **createdAt:** Instant
- **comments:** `List<Comment>` (сущности, часть агрегата Task)
- **files:** `List<FileAttachment>` (сущности, часть агрегата Task)
- **orderInColumn:** Int (позиция в колонке)

**Правила:**
- Задача может быть в одном из состояний: backlog, column (colId), archive
- При создании помещается в бэклог
- Приоритет влияет на сортировку в бэклоге (high → critical → medium → low)
- Назначение исполнителя проверяет лимит исполнителей по тарифу
- Use Case — поле для группировки задач в свимлайны на фронте (см. `project-and-board`)

## Сущности

### `Comment` (entity, часть Task)
Комментарий к задаче.

- **id:** `CommentId` (value object)
- **authorId:** `UserId`
- **text:** String (1-5000)
- **createdAt:** Instant
- **updatedAt:** Instant?

**Правила:**
- Редактировать/удалять комментарий может только автор

### `FileAttachment` (entity, часть Task)
Прикреплённый файл к задаче.

- **id:** `FileId` (value object)
- **name:** String (имя файла)
- **size:** Long (байты, макс. 10 MB)
- **storageKey:** String (путь в MinIO)
- **uploadedAt:** Instant

**Правила:**
- Файл хранится в MinIO, в БД — только метаданные
- presigned URL выдаётся для прямой загрузки/скачивания между клиентом и MinIO

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `TaskId` | `value: UUID` | Не null |
| `CommentId` | `value: UUID` | Не null |
| `FileId` | `value: UUID` | Не null |
| `Priority` | `enum: LOW, MEDIUM, HIGH, CRITICAL` | — |
| `TaskStatus` | `Backlog | Column(ColumnId) | Archive` | — |
| `Label` | `value: String` | 1-50 символов |
| `StorageKey` | `value: String` | Формат: `projects/{projectId}/tasks/{taskId}/{filename}` |

## Операции

| Операция | Команда | Агрегат | Событие |
|---|---|---|---|
| Создание задачи | `CreateTask(projectId, title, ...)` | Task | `TaskCreated` |
| Просмотр задачи | `GetTask(taskId)` | Task | — (query) |
| Редактирование задачи | `UpdateTask(taskId, title?, description?, ...)` | Task | `TaskUpdated` |
| Перемещение задачи | `MoveTask(taskId, targetStatus)` | Task | `TaskMoved` |
| Архивирование | `ArchiveTask(taskId)` | Task | `TaskArchived` |
| Сортировка в колонке | `ReorderTask(taskId, position)` | Task | `TaskReordered` |
| Назначение исполнителя | `AssignUser(taskId, assigneeId)` | Task | `TaskAssigneeChanged` |
| Добавление комментария | `AddComment(taskId, authorId, text)` | Task (через Task) | `CommentAdded` |
| Редактирование комментария | `EditComment(taskId, commentId, text)` | Task | `CommentUpdated` |
| Удаление комментария | `DeleteComment(taskId, commentId)` | Task | `CommentDeleted` |
| Прикрепление файла | `AttachFile(taskId, file)` | Task | `FileAttached` |
| Удаление файла | `DetachFile(taskId, fileId)` | Task | `FileDetached` |
| Запрос presigned URL | `GetPresignedUrl(taskId, fileId, action)` | FileAttachment | — (query) |

## Связанные Use Case
- [UC-04-01](../usecase/04-task-operations.md#uc-04-01) … [UC-04-10](../usecase/04-task-operations.md#uc-04-10)

## Связанные API
- `docs/ai/api/task.md` — 8 эндпоинтов
- `docs/ai/api/comment.md` — 4 эндпоинта
