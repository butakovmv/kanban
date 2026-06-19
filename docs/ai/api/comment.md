# API комментариев

Базовый путь: `/api/v1/projects/{id}/tasks/{taskId}/comments`

## Эндпоинты

### `GET /api/v1/projects/{id}/tasks/{taskId}/comments`
Список комментариев к задаче.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?offset=0&limit=50&sort=asc|desc` (по умолчанию `asc` — от старых к новым)
- **Ответ:** `200 OK` — `{ "items": [ { "id", "author": { "id", "login" }, "text", "createdAt", "updatedAt?" } ], "total": int }`

### `POST /api/v1/projects/{id}/tasks/{taskId}/comments`
Добавление комментария.

- **Доступ:** участник проекта (`task:update`)
- **Тело:** `{ "text": "string (1-5000)" }`
- **Ответ:** `201 Created` — `{ "id": "uuid", "createdAt": "datetime" }`
- **Ошибки:** `401`, `403`

### `PUT /api/v1/projects/{id}/tasks/{taskId}/comments/{cmtId}`
Редактирование комментария.

- **Доступ:** автор комментария
- **Тело:** `{ "text": "string (1-5000)" }`
- **Ответ:** `200 OK`
- **Ошибки:** `401`, `403` — не автор комментария

### `DELETE /api/v1/projects/{id}/tasks/{taskId}/comments/{cmtId}`
Удаление комментария.

- **Доступ:** автор комментария
- **Ответ:** `204 No Content`
- **Ошибки:** `401`, `403` — не автор комментария

## Связанные Use Case

- [UC-04-07](../../usecase/04-task-operations.md#uc-04-07) — добавление комментария
