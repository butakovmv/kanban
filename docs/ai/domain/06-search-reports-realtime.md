# Домен: Поиск, Отчёты и Real-time (Search, Reports & Realtime)

## Обзор
Три смежных вспомогательных контекста: полнотекстовый поиск по задачам, аналитические отчёты (CFD, Lead Time, Гантт, лог событий) и синхронизация изменений в реальном времени через SSE.

**Границы:**
- Не хранят собственных агрегатов (кроме Event); работают с данными из других контекстов через query / event subscription
- Поиск использует PostgreSQL full-text search
- Realtime использует in-memory `Sinks.Many` для публикации событий

## Агрегаты

### `BoardEvent` (корень агрегата, только для realtime)
Событие изменения на доске.

- **id:** `EventId` (value object)
- **projectId:** `ProjectId`
- **type:** `EventType` (value object)
- **payload:** JSON (зависит от типа)
- **userId:** `UserId` (инициатор)
- **timestamp:** Instant

**Правила:**
- Событие публикуется после успешной команды в других контекстах
- Буфер последних N событий хранится для восполнения по Last-Event-ID
- Событие не является persistent (только in-memory буфер)

## Сущности

(отдельные сущности не выделяются; `BoardEvent` — единственный агрегат)

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `EventId` | `value: Long` | Монотонно возрастающий |
| `EventType` | `enum: TASK_CREATED, TASK_MOVED, TASK_UPDATED, TASK_ARCHIVED, COLUMN_CREATED, COLUMN_UPDATED, COLUMN_DELETED, COLUMN_REORDERED, COMMENT_ADDED, COMMENT_DELETED` | — |
| `SearchQuery` | `q: String, includeBacklog: Boolean, includeArchive: Boolean, includeComments: Boolean` | q.length >= 2 |
| `CfdQuery` | `from: LocalDate, to: LocalDate, granularity: DAY|WEEK` | from <= to |
| `LeadTimeQuery` | `from: LocalDate, to: LocalDate, buckets: Int` | buckets 5-50 |
| `GanttData` | `tasks: List, useCases: List` | — (query result) |
| `CfdSeries` | `columns: List, series: List` | — (query result) |
| `LeadTimeBucket` | `range: String, count: Int` | count >= 0 |

## Операции

| Операция | Команда | Контекст | Событие |
|---|---|---|---|
| Поиск задач | `SearchTasks(projectId, query)` | Search | — (query) |
| CFD отчёт | `GetCfd(projectId, from, to, granularity)` | Reports | — (query) |
| Lead Time | `GetLeadTime(projectId, from, to, buckets)` | Reports | — (query) |
| Гантт | `GetGantt(projectId)` | Reports | — (query) |
| Лог событий | `GetEventLog(projectId, limit, offset)` | Reports | — (query) |
| Публикация события | `PublishEvent(projectId, type, payload, userId)` | Realtime | (публикуется в SSE) |
| Подписка на SSE | `SubscribeToProject(projectId, lastEventId?)` | Realtime | — (stream) |
| Восполнение пропущенных | `ReplayMissedEvents(projectId, lastEventId)` | Realtime | — (query + stream) |

## Связанные Use Case
- [UC-05-01](../usecase/05-filtering-and-search.md#uc-05-01) … [UC-05-04](../usecase/05-filtering-and-search.md#uc-05-04) — фильтрация и поиск
- [UC-07-01](../usecase/07-reports.md#uc-07-01) … [UC-07-04](../usecase/07-reports.md#uc-07-04) — отчёты
- [UC-09-01](../usecase/09-realtime.md#uc-09-01) … [UC-09-02](../usecase/09-realtime.md#uc-09-02) — real-time синхронизация

## Связанные API
- `docs/ai/api/search.md` — 1 эндпоинт
- `docs/ai/api/report.md` — 4 эндпоинта
- `docs/ai/api/realtime.md` — 1 SSE-эндпоинт
