# API управления доступом

Базовый путь: `/api/v1/projects/{id}/groups`

## Эндпоинты

### `GET /api/v1/projects/{id}/groups`
Список групп проекта.

- **Доступ:** владелец проекта
- **Ответ:** `200 OK` — `{ "groups": [ { "id", "name", "memberCount": int, "permissions": { "task:read", "task:create", "task:update", "task:delete", "column:create", "column:update", "column:delete", "document:read", "document:create", "document:update", "document:delete" } } ] }`

### `POST /api/v1/projects/{id}/groups`
Создание группы.

- **Доступ:** владелец проекта
- **Тело:** `{ "name": "string (1-100)" }`
- **Ответ:** `201 Created` — `{ "id": "uuid", "name" }`

### `DELETE /api/v1/projects/{id}/groups/{gId}`
Удаление группы.

- **Доступ:** владелец проекта
- **Ответ:** `204 No Content`
- **Ошибки:** `401`, `403`

### `POST /api/v1/projects/{id}/groups/{gId}/members`
Добавление пользователя в группу.

- **Доступ:** владелец проекта
- **Тело:** `{ "loginOrEmail": "string (макс. 254)" }`
- **Ответ:** `201 Created` — `{ "userId": "uuid", "login": "string" }`
- **Ошибки:** `401`, `403`, `404` — пользователь не найден

### `DELETE /api/v1/projects/{id}/groups/{gId}/members/{uId}`
Удаление пользователя из группы.

- **Доступ:** владелец проекта
- **Ответ:** `204 No Content`
- **Ошибки:** `401`, `403`

### `PUT /api/v1/projects/{id}/groups/{gId}/permissions`
Настройка прав доступа группы.

- **Доступ:** владелец проекта
- **Тело:** `{ "permissions": { "task:read": bool, "task:create": bool, "task:update": bool, "task:delete": bool, "column:create": bool, "column:update": bool, "column:delete": bool, "document:read": bool, "document:create": bool, "document:update": bool, "document:delete": bool } }`
- **Ответ:** `200 OK`
- **Ошибки:** `401`, `403`

## Связанные Use Case

- [UC-08-01](../../usecase/08-access-control.md#uc-08-01) — создание группы
- [UC-08-02](../../usecase/08-access-control.md#uc-08-02) — добавление пользователя
- [UC-08-03](../../usecase/08-access-control.md#uc-08-03) — удаление пользователя
- [UC-08-04](../../usecase/08-access-control.md#uc-08-04) — настройка прав
