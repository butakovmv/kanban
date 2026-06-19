# API отчётов

Базовый путь: `/api/v1/projects/{id}/reports`

## Эндпоинты

### `GET /api/v1/projects/{id}/reports/cfd`
Данные для Cumulative Flow Diagram.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?from=date&to=date&granularity=day|week`
- **Ответ:** `200 OK` — `{ "columns": [ { "id", "name" } ], "series": [ { "date": "date", "counts": { colId: int } } ] }`

### `GET /api/v1/projects/{id}/reports/lead-time`
Данные для Lead Time Distribution Chart.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?from=date&to=date&buckets=10` (количество интервалов гистограммы)
- **Ответ:** `200 OK` — `{ "buckets": [ { "range": "0-2 days", "count": int } ] }`

### `GET /api/v1/projects/{id}/reports/gantt`
Данные для диаграммы Гантта по Use Case.

- **Доступ:** участник проекта (`task:read`)
- **Ответ:** `200 OK` — `{ "tasks": [ { "id", "title", "useCase", "createdAt", "workStartDate?", "archivedAt?" } ], "useCases": [ "string" ] }`

### `GET /api/v1/projects/{id}/reports/events`
Лог событий доски.

- **Доступ:** участник проекта (`task:read`)
- **Параметры:** `?limit=100&offset=0&from=date&to=date`
- **Ответ:** `200 OK` — `{ "items": [ { "id", "type": "TASK_CREATED" | "TASK_MOVED" | "TASK_UPDATED" | "COLUMN_CREATED" | "COLUMN_DELETED" | "COMMENT_ADDED" | "COMMENT_DELETED" | ..., "payload": {}, "userId": "uuid", "timestamp": "datetime" } ], "total": int }`

## Связанные Use Case

- [UC-07-01](../../usecase/07-reports.md#uc-07-01) — CFD
- [UC-07-02](../../usecase/07-reports.md#uc-07-02) — Lead Time
- [UC-07-03](../../usecase/07-reports.md#uc-07-03) — Гантт
- [UC-07-04](../../usecase/07-reports.md#uc-07-04) — лог событий
