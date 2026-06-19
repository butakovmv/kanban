# API задач

Базовый путь: `/api/v1/projects/{id}/tasks`

## Эндпоинты

### `POST /api/v1/projects/{id}/tasks`
Создание задачи. Помещается в бэклог.

- **Доступ:** участник проекта (`task:create`)
- **Тело:** `{ "title": "string (1-500)", "description?": "string (макс. 10000)", "useCase?": "string (1-200)", "priority": "low" | "medium" | "high" | "critical", "labels?": [ "string (1-50)" ], "assigneeId?": "uuid" }`
- **Ответ:** `201 Created` — `{ "id": "uuid", "createdAt": "datetime", "status": "backlog" }`
- **Ошибки:** `401`, `403` — недостаточно прав

### `GET /api/v1/projects/{id}/tasks/{taskId}`
Полная информация о задаче.

- **Доступ:** участник проекта (`task:read`)
- **Ответ:** `200 OK` — `{ "id", "title", "description", "useCase", "priority", "status", "labels", "assignee": { "id", "login" }, "createdAt", "workStartDate?", "archivedAt?", "files": [ { "id", "name", "size" } ], "commentCount": int }`
- **Ошибки:** `401`, `403`, `404`

### `PUT /api/v1/projects/{id}/tasks/{taskId}`
Обновление полей задачи.

- **Доступ:** участник проекта (`task:update`)
- **Тело:** `{ "title?": "string (1-500)", "description?": "string (макс. 10000)", "useCase?": "string (1-200)", "priority?": "low" | "medium" | "high" | "critical", "labels?": [ "string (1-50)" ], "assigneeId?": "uuid" | null }`
- **Ответ:** `200 OK`
- **Ошибки:** `401`, `403`, `404`, `422` — нарушение лимита тарифа

### `PATCH /api/v1/projects/{id}/tasks/{taskId}/status`
Перемещение задачи между колонками (drag-n-drop или через выбор статуса).

- **Доступ:** участник проекта (`task:update`)
- **Тело:** `{ "status": "uuid" | "backlog" | "archive" }` — ID целевой колонки или ключевое значение
- **Ответ:** `200 OK`
- **Примечание:** при первом перемещении задачи из бэклога в колонку сервер фиксирует дату взятия в работу. Свимлайны управляются на фронте ([UC-03-06](../../usecase/03-board-columns-and-swimlanes.md#uc-03-06)).

### `POST /api/v1/projects/{id}/tasks/{taskId}/archive`
Архивирование задачи.

- **Доступ:** участник проекта (`task:update`)
- **Ответ:** `200 OK` — `{ "archivedAt": "datetime" }`
- **Примечание:** эквивалентно `PATCH .../status` со значением `"archive"`. Выделено для удобства UI-кнопки.

### `POST /api/v1/projects/{id}/tasks/{taskId}/files`
Прикрепление файла к задаче.

- **Доступ:** участник проекта (`task:update`)
- **Тело:** `multipart/form-data` — поле `file` с содержимым файла (макс. 10 MB)
- **Ответ:** `201 Created` — `{ "fileId": "uuid", "name": "string", "size": int }`
- **Примечание:** сервер загружает файл в MinIO, записывает метаданные в БД.

### `DELETE /api/v1/projects/{id}/tasks/{taskId}/files/{fileId}`
Удаление прикреплённого файла.

- **Доступ:** участник проекта (`task:update`)
- **Ответ:** `204 No Content`

### `GET /api/v1/projects/{id}/tasks/{taskId}/files/{fileId}/presigned-url`
Получение presigned URL для загрузки/скачивания файла напрямую из MinIO.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?action=download` (по умолчанию) или `?action=upload`
- **Ответ:** `200 OK` — `{ "url": "https://...", "expiresIn": 3600 }`
- **Примечание:** при `action=download` ссылка действительна для GET-запроса; при `action=upload` — для PUT-запроса

## Связанные Use Case

- [UC-04-01](../../usecase/04-task-operations.md#uc-04-01) — создание задачи
- [UC-04-02](../../usecase/04-task-operations.md#uc-04-02) — просмотр задачи
- [UC-04-03](../../usecase/04-task-operations.md#uc-04-03) — перемещение drag-n-drop
- [UC-04-04](../../usecase/04-task-operations.md#uc-04-04) — перемещение через статус
- [UC-04-05](../../usecase/04-task-operations.md#uc-04-05) — сортировка (board.md)
- [UC-04-06](../../usecase/04-task-operations.md#uc-04-06) — редактирование задачи
- [UC-04-08](../../usecase/04-task-operations.md#uc-04-08) — прикрепление файла
- [UC-04-09](../../usecase/04-task-operations.md#uc-04-09) — архивирование
- [UC-04-10](../../usecase/04-task-operations.md#uc-04-10) — назначение исполнителя
