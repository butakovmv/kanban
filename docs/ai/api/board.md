# API доски, колонок, бэклога и архива

Базовый путь: `/api/v1/projects/{id}`

## Эндпоинты

### `GET /api/v1/projects/{id}/board`
Полное состояние доски: колонки, задачи в колонках с фильтром. Свимлайны группируются на фронте по полю `useCase` задач.

- **Доступ:** участник проекта (`task:read`)
- **Ответ:** `200 OK` — `{ "columns": [ { "id", "name", "order", "tasks": [ { "id", "title", "useCase", "priority", "assignee", "labels" } ] } ] }`
- **Примечание:** бэклог и архив не включены в ответ — запрашиваются отдельно. Свимлайны не возвращаются — фронт группирует задачи по полю `useCase`

### `POST /api/v1/projects/{id}/columns`
Создание колонки.

- **Доступ:** участник проекта (`column:create`)
- **Тело:** `{ "name": "string (1-200)" }`
- **Ответ:** `201 Created` — `{ "id": "uuid", "name", "order": int }`
- **Ошибки:** `401`, `403` — превышен лимит колонок или недостаточно прав

### `PUT /api/v1/projects/{id}/columns/{colId}`
Редактирование колонки (переименование).

- **Доступ:** участник проекта (`column:update`)
- **Тело:** `{ "name": "string (1-200)" }`
- **Ответ:** `200 OK`
- **Ошибки:** `401`, `403`, `404`

### `DELETE /api/v1/projects/{id}/columns/{colId}`
Удаление колонки. Задачи переносятся в бэклог.

- **Доступ:** участник проекта (`column:delete`)
- **Ответ:** `204 No Content`
- **Ошибки:** `401`, `403`, `404`

### `PUT /api/v1/projects/{id}/columns/{colId}/order`
Сохранение порядка задач в колонке (drag-n-drop).

- **Доступ:** участник проекта (`task:update`)
- **Тело:** `{ "taskIds": [ "uuid", ... ] }` — полный упорядоченный список ID задач
- **Ответ:** `200 OK`

### `GET /api/v1/projects/{id}/backlog`
Загрузка задач из бэклога.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?filter[priority]=...&filter[assignee]=...` (при активном фильтре доски)
- **Ответ:** `200 OK` — `{ "tasks": [ { "id", "title", "useCase", "priority", "assignee", "labels", "createdAt" } ] }`
- **Примечание:** сортировка по приоритету (от высокого к низкому)

### `GET /api/v1/projects/{id}/archive`
Загрузка задач из архива.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?offset=0&limit=50&filter[priority]=...`
- **Ответ:** `200 OK` — `{ "tasks": [...], "total": int }`
- **Примечание:** сортировка по дате архивирования (от новых к старым)

## Связанные Use Case

- [UC-03-01](../../usecase/03-board-columns-and-swimlanes.md#uc-03-01) — создание колонки
- [UC-03-02](../../usecase/03-board-columns-and-swimlanes.md#uc-03-02) — редактирование колонки
- [UC-03-03](../../usecase/03-board-columns-and-swimlanes.md#uc-03-03) — удаление колонки
- [UC-03-04](../../usecase/03-board-columns-and-swimlanes.md#uc-03-04) — открытие/скрытие бэклога (client-only)
- [UC-03-05](../../usecase/03-board-columns-and-swimlanes.md#uc-03-05) — открытие/скрытие архива (client-only)
- [UC-03-06](../../usecase/03-board-columns-and-swimlanes.md#uc-03-06) — авто-группировка свимлайнов (client-only)
- [UC-03-07](../../usecase/03-board-columns-and-swimlanes.md#uc-03-07) — авто-удаление свимлайнов (client-only)
- [UC-04-05](../../usecase/04-task-operations.md#uc-04-05) — сортировка задач в колонке
- [UC-05-01](../../usecase/05-filtering-and-search.md#uc-05-01) — фильтрация задач
