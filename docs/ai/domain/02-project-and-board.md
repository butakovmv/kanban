# Домен: Проект и Доска (Project & Board)

## Обзор
Управление проектами и структурой канбан-доски. Проект — корневой агрегат, владеющий доской (колонки, бэклог, архив). Задачи входят в состав доски, но управляются отдельным контекстом (`task`).

Свимлайны — клиентская группировка задач по полю Use Case, управляются исключительно на фронте. Сервер не хранит и не управляет свимлайнами.

**Границы:**
- Задачи не входят в этот контекст (только их идентификаторы и порядок)
- Документы проекта — отдельный контекст (`document`)
- Права доступа к проекту — отдельный контекст (`access-control`)
- Свимлайны — frontend-only, сервер о них не знает

## Агрегаты

### `Project` (корень агрегата)
Проект пользователя.

- **id:** `ProjectId` (value object)
- **ownerId:** `UserId` (value object)
- **name:** String (1-200)
- **description:** String? (макс. 2000)
- **createdAt:** Instant
- **board:** `Board` (встроенный value object / entity, часть агрегата Project)

**Правила:**
- Проект удаляется каскадно со всеми задачами, колонками, документами
- Владелец — единственный, кто может удалять/редактировать проект
- Один пользователь не может превысить лимит проектов по тарифу

### `Board` (value object, часть Project)
Структура доски.

- **columns:** `List<Column>` (всегда 1+ колонка)
- **backlogVisibility:** Visibility (открыт/скрыт)
- **archiveVisibility:** Visibility

**Правила:**
- При создании проекта доска инициализируется с тремя колонками: To Do, In Progress, Done
- Порядок колонок хранится как последовательность идентификаторов
- Свимлайны на сервере не хранятся — фронт группирует задачи по полю `useCase`

## Сущности

### `Column` (entity, часть Board)
Колонка статуса на доске.

- **id:** `ColumnId` (value object)
- **name:** String (1-200)
- **order:** Int (позиция на доске)
- **taskIds:** `List<TaskId>` (упорядоченный список задач в колонке)

**Правила:**
- При удалении колонки все задачи переносятся в бэклог
- Лимит колонок определяется тарифом

### `Swimlane` (entity, frontend-only)
Группировка задач по Use Case. Управляется исключительно на фронте; сервер не имеет сущности `Swimlane` и не участвует в её создании/удалении.

- **useCase:** String (1-200) — поле задачи, по которому фронт группирует карточки
- **label:** String — заголовок свимлайна (совпадает со значением useCase или «Bug Fix» для задач без useCase)

**Правила (frontend-only):**
- Фронт группирует задачи в колонках по полю `useCase`
- Каждое уникальное значение `useCase` среди задач в колонках образует отдельный свимлайн
- Задачи без `useCase` (null) попадают в дефолтный свимлайн «Bug Fix»
- Новый свимлайн автоматически появляется, когда в колонках появляется задача с новым `useCase`
- Свимлайн исчезает, когда последняя задача с этим `useCase` покидает колонки (уходит в архив или бэклог)
- Порядок свимлайнов определяется порядком появления первого соответствующего `useCase` (или сортировкой на фронте)

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `ProjectId` | `value: UUID` | Не null |
| `ColumnId` | `value: UUID` | Не null |
| `TaskId` | `value: UUID` | Не null |
| `Visibility` | `enum: HIDDEN, VISIBLE` | — |
| `TaskCounts` | `backlog: Int, columns: Map<ColumnId, Int>, archive: Int` | Все >= 0 |

## Операции

| Операция | Команда | Агрегат | Событие |
|---|---|---|---|
| Создание проекта | `CreateProject(ownerId, name, description?)` | Project | `ProjectCreated` |
| Редактирование проекта | `UpdateProject(projectId, name?, description?)` | Project | `ProjectUpdated` |
| Удаление проекта | `DeleteProject(projectId)` | Project | `ProjectDeleted` |
| Создание колонки | `AddColumn(projectId, name)` | Board (через Project) | `ColumnCreated` |
| Редактирование колонки | `RenameColumn(projectId, columnId, name)` | Board | `ColumnUpdated` |
| Удаление колонки | `DeleteColumn(projectId, columnId)` | Board | `ColumnDeleted` |
| Сортировка колонок | `ReorderColumns(projectId, columnIds)` | Board | `ColumnsReordered` |
| Сортировка задач в колонке | `ReorderTasksInColumn(projectId, columnId, taskIds)` | Board | `ColumnReordered` |

## Связанные Use Case
- [UC-02-01](../usecase/02-project-management.md#uc-02-01) … [UC-02-03](../usecase/02-project-management.md#uc-02-03)
- [UC-03-01](../usecase/03-board-columns-and-swimlanes.md#uc-03-01) … [UC-03-05](../usecase/03-board-columns-and-swimlanes.md#uc-03-05) — колонки, бэклог, архив

## Связанные API
- `docs/ai/api/project.md` — 5 эндпоинтов
- `docs/ai/api/board.md` — 7 эндпоинтов
