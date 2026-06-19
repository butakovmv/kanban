# API поиска

Базовый путь: `/api/v1/projects/{id}/search`

## Эндпоинты

### `GET /api/v1/projects/{id}/search`
Поиск задач по тексту.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:**
  - `q` (string, обязательный) — текст поиска
  - `includeBacklog` (bool, по умолчанию `true`) — искать в бэклоге
  - `includeArchive` (bool, по умолчанию `false`) — искать в архиве
  - `includeComments` (bool, по умолчанию `false`) — искать также в комментариях
  - `offset` (int, по умолчанию `0`)
  - `limit` (int, по умолчанию `50`)
- **Ответ:** `200 OK` — `{ "items": [ { "id", "title", "description?": "string (snippet)", "status", "priority", "assignee": { "id", "login" }, "useCase?" } ], "total": int }`
- **Примечание:** поиск выполняется по названию и описанию задач (и опционально по тексту комментариев). Совпадения подсвечиваются на фронте.

## Связанные Use Case

- [UC-05-03](../../usecase/05-filtering-and-search.md#uc-05-03) — поиск по задачам
- [UC-05-04](../../usecase/05-filtering-and-search.md#uc-05-04) — расширенный поиск
