# API real-time событий (SSE)

## SSE-эндпоинт

### `GET /api/v1/projects/{id}/events`
Установка SSE-соединения для получения событий доски в реальном времени.

- **Доступ:** участник проекта (`task:read`)
- **Заголовки:** `Authorization: Bearer <token>`
- **Ответ:** `200 OK` — `Content-Type: text/event-stream`
- **Протокол:** [Server-Sent Events](https://html.spec.whatwg.org/multipage/server-sent-events.html)

### Формат событий

Каждое событие — строка с типом и JSON-данными:

```
event: task_moved
data: {"type":"TASK_MOVED","payload":{"taskId":"uuid","fromColumnId":"uuid","toColumnId":"uuid","position":int},"userId":"uuid","timestamp":"2026-06-19T12:00:00Z"}

event: task_updated
data: {"type":"TASK_UPDATED","payload":{"taskId":"uuid","changedFields":["priority","assignee"]},"userId":"uuid","timestamp":"..."}

event: task_created
data: {"type":"TASK_CREATED","payload":{"task":{...}},"userId":"uuid","timestamp":"..."}

event: task_archived
data: {"type":"TASK_ARCHIVED","payload":{"taskId":"uuid","archivedAt":"..."},"userId":"uuid","timestamp":"..."}

event: column_created
data: {"type":"COLUMN_CREATED","payload":{"column":{...}},"userId":"uuid","timestamp":"..."}

event: column_updated
data: {"type":"COLUMN_UPDATED","payload":{"columnId":"uuid","name":"..."},"userId":"uuid","timestamp":"..."}

event: column_deleted
data: {"type":"COLUMN_DELETED","payload":{"columnId":"uuid"},"userId":"uuid","timestamp":"..."}

event: column_reordered
data: {"type":"COLUMN_REORDERED","payload":{"columnId":"uuid","taskIds":["uuid",...]},"userId":"uuid","timestamp":"..."}

event: comment_added
data: {"type":"COMMENT_ADDED","payload":{"taskId":"uuid","comment":{...}},"userId":"uuid","timestamp":"..."}

event: comment_deleted
data: {"type":"COMMENT_DELETED","payload":{"taskId":"uuid","commentId":"uuid"},"userId":"uuid","timestamp":"..."}
```

### Автоматическое переподключение

- Клиент использует браузерный `EventSource`
- При разрыве браузер автоматически отправляет `Last-Event-ID`
- Сервер хранит буфер последних N событий; при получении `Last-Event-ID` сервер воспроизводит пропущенные события
- При длительном разрыве (> 30 сек) клиент выполняет полную перезагрузку доски через `GET /api/v1/projects/{id}/board`

## Связанные Use Case

- [UC-09-01](../../usecase/09-realtime.md#uc-09-01) — синхронизация изменений
- [UC-09-02](../../usecase/09-realtime.md#uc-09-02) — конкурирующие изменения (server-side resolution)
