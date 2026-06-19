# План реализации (Implementation Plan)

## Соглашения

| Параметр | Значение |
|---|---|---|
| Разработчик | Middle-разработчик (бэкенд или фронтенд) |
| Тестировщик-автоматизатор | Middle, Playwright + TypeScript, пишет тесты параллельно с разработкой |
| Размер задачи | 2–4 часа чистой работы |
| Длительность дня | 8 часов |
| Параллельность | Бэкенд + фронтенд + тестировщик работают параллельно |
| Базовый стек | Kotlin, Spring WebFlux, R2DBC, PostgreSQL, MinIO, Vue 3, Vite, Playwright |
| Сборка | Gradle (Kotlin DSL), мультимодульный проект |

---

## Иерархический план работ (WBS)

### 0. Инфраструктура и каркас

```
0.1  Корневой Gradle-билд
0.2  Модуль spring — точка входа, Actuator, Security (JWT), Dockerfile
0.3  Модуль usecase — пустой модуль с domain-пакетами
0.4  Модуль webapi — пустой модуль, WebFlux-конфигурация, CORS
0.5  Модуль postgres — пустой модуль, R2DBC-конфигурация, Flyway
0.6  Модуль nats — пустой модуль (опциональная заглушка)
0.7  Docker Compose — postgres, minio, nginx (dev-стенд)
0.8  Vue-каркас — Vite + Router + Pinia + SCSS-темы + fetch-клиент
T0.1  [ТА] Playwright-проект (e2etest) — настройка конфига, окружения, CI-скрипта
T0.2  [ТА] Фабрики тестовых данных (генераторы пользователей, проектов, задач)
```

### 1. Идентификация и аутентификация

```
1.1  usecase: User, Tariff — сущности, value objects, порты репозитория
1.2  usecase: RegisterUser + Login + LoginWithTotp — операции
1.3  usecase: BindTotp + VerifyTotp + UnbindTotp — операции
1.4  usecase: RequestRecovery + ResetPassword — операции
1.5  usecase: Logout + GetSession + GetTariff — операции
1.6  postgres: UserTable + TariffTable + RecoveryTokenTable — реализация репозиториев
1.7  webapi: AuthController — register, login, session, tariff (+ JWT-фильтр)
1.8  webapi: TotpController + RecoveryController
1.9  vue: LoginPage, RegisterPage, auth-store
1.10 vue: ProfilePage (TOTP привязка, тариф)
T1.1  [ТА] API-тесты auth (register, login, TOTP, recover, session) + UI-тесты (LoginPage, RegisterPage)
```

### 2. Проект и доска

```
2.1  usecase: Project + Column — сущности, порты, операции
2.2  postgres: ProjectTable + ColumnTable — реализация репозиториев
2.3  webapi: ProjectController — CRUD проектов
2.4  vue: ProjectListPage + ProjectSettingsPage
2.5  usecase: Board — запрос состояния + авто-создание колонок (при создании проекта)
2.6  webapi: BoardController + BacklogController + ArchiveController
2.7  vue: BoardPage — рендер колонок, панели бэклога/архива
2.8  vue: Создание/редактирование/удаление колонок (UI)
2.9  vue: Свимлайны — frontend-only группировка по useCase
T2.1  [ТА] API-тесты project/board (CRUD, backlog, archive) + UI-тесты (BoardPage, DnD)
```

### 3. Задачи, комментарии, файлы

```
3.1  usecase: Task — сущность, CreateTask + GetTask
3.2  usecase: UpdateTask + MoveTask + ArchiveTask
3.3  usecase: Comment — сущность, CRUD-операции
3.4  usecase: FileAttachment — сущность, AttachFile + DetachFile + GetPresignedUrl
3.5  postgres: TaskTable + CommentTable + FileTable — репозитории
3.6  webapi: TaskController — CRUD + PATCH /status + POST /archive
3.7  webapi: CommentController — CRUD
3.8  webapi: FileController — upload (multipart), presigned URL, delete
3.9  vue: TaskCard (минималистичная карточка), TaskCreateForm
3.10 vue: Drag-n-drop между колонками (board logic)
3.11 vue: TaskDetailPage — полная информация, комменты, файлы
3.12 vue: CommentSystem + FileUpload UI
T3.1  [ТА] API-тесты task/comment/file + UI-тесты (TaskDetail, DnD, файлы)
```

### 4. Документы

```
4.1  usecase + postgres: Document — сущность, порт, репозиторий
4.2  webapi: DocumentController — list, get, upload, update, delete
4.3  vue: DocumentListPage (группировка по типам)
4.4  vue: DocumentUploadDialog — загрузка/замена файла
4.5  MinIO: интеграция presigned URL для документов (дополнение к задаче 3.8)
T4.1  [ТА] API-тесты document + UI-тесты (DocumentList, загрузка)
```

### 5. Управление доступом

```
5.1  usecase + postgres: Group + Permission — сущность, порт, репозиторий
5.2  usecase + postgres: Member — add/remove, проверка прав
5.3  webapi: AccessController — группы, участники, права
5.4  webapi: PermissionCheck — middleware/helper для всех контроллеров
5.5  vue: AccessControlPage — список групп, участники
5.6  vue: PermissionEditor — визуальный редактор прав
T5.1  [ТА] API-тесты access control + UI-тесты (AccessControlPage, PermissionEditor)
```

### 6. Поиск и отчёты

```
6.1  usecase + postgres: Search — full-text поиск по задачам
6.2  webapi: SearchController
6.3  vue: SearchPage — поле поиска, результаты, сниппеты, фильтры
6.4  usecase + postgres: Reports — CFD, Lead Time, Gantt, EventLog
6.5  webapi: ReportController — 4 эндпоинта
6.6  vue: ReportsPage — диаграммы (Chart.js или аналог)
T6.1  [ТА] API-тесты search/reports + UI-тесты (SearchPage, ReportsPage)
```

### 7. Real-time синхронизация (SSE)

```
7.1  webapi: SSE endpoint + Sinks.Many + буфер последних событий
7.2  webapi: Интеграция публикации событий во все мутирующие контроллеры
7.3  vue: EventSource — подключение, обработка событий, обновление store
7.4  vue: Оптимистичные обновления + обработка конфликтов (Last-Event-ID)
T7.1  [ТА] Тесты real-time (SSE-события, синхронизация между вкладками)
```

### 8. Финализация

```
8.1  Docker Compose для production (Nginx, sticky-session, healthcheck)
8.2  Деплой на стенд (1 день)
8.3  Исправление замечаний (1 день)
8.4  Приёмочные тесты (1 день)
```

---

## Граф зависимостей

```mermaid
flowchart LR
    0_1["0.1 Gradle-билд"]
    0_2["0.2 spring-модуль"]
    0_3["0.3 usecase-модуль"]
    0_4["0.4 webapi-модуль"]
    0_5["0.5 postgres-модуль"]
    0_6["0.6 nats-модуль"]
    0_7["0.7 Docker Compose"]
    0_8["0.8 Vue-каркас"]

    1_1["1.1 usecase: User + Tariff"]
    1_2["1.2 usecase: Register + Login"]
    1_3["1.3 usecase: TOTP"]
    1_4["1.4 usecase: Recovery"]
    1_5["1.5 usecase: Logout + Session"]
    1_6["1.6 postgres: User + Tariff"]
    1_7["1.7 webapi: Auth"]
    1_8["1.8 webapi: TOTP + Recovery"]
    1_9["1.9 vue: Login + Register"]
    1_10["1.10 vue: Profile + TOTP"]

    2_1["2.1 usecase: Project + Column"]
    2_2["2.2 postgres: Project + Column"]
    2_3["2.3 webapi: Project"]
    2_4["2.4 vue: ProjectList + Settings"]
    2_5["2.5 usecase: Board"]
    2_6["2.6 webapi: Board + Backlog + Archive"]
    2_7["2.7 vue: BoardPage layout"]
    2_8["2.8 vue: Column CRUD"]
    2_9["2.9 vue: Swimlane"]

    3_1["3.1 usecase: Task (create + get)"]
    3_2["3.2 usecase: Task (update + move + archive)"]
    3_3["3.3 usecase: Comment"]
    3_4["3.4 usecase: FileAttachment"]
    3_5["3.5 postgres: Task + Comment + File"]
    3_6["3.6 webapi: Task"]
    3_7["3.7 webapi: Comment"]
    3_8["3.8 webapi: File"]
    3_9["3.9 vue: TaskCard + Create"]
    3_10["3.10 vue: DnD columns"]
    3_11["3.11 vue: TaskDetail"]
    3_12["3.12 vue: Comments + Files"]

    4_1["4.1 usecase+pg: Document"]
    4_2["4.2 webapi: Document"]
    4_3["4.3 vue: DocumentList"]
    4_4["4.4 vue: DocumentUpload"]
    4_5["4.5 MinIO интеграция"]

    5_1["5.1 usecase+pg: Group + Permission"]
    5_2["5.2 usecase+pg: Members"]
    5_3["5.3 webapi: Access"]
    5_4["5.4 webapi: PermissionCheck"]
    5_5["5.5 vue: AccessControl"]
    5_6["5.6 vue: PermissionEditor"]

    6_1["6.1 usecase+pg: Search"]
    6_2["6.2 webapi: Search"]
    6_3["6.3 vue: SearchPage"]
    6_4["6.4 usecase+pg: Reports"]
    6_5["6.5 webapi: Reports"]
    6_6["6.6 vue: ReportsPage"]

    7_1["7.1 webapi: SSE endpoint"]
    7_2["7.2 webapi: Publish events"]
    7_3["7.3 vue: EventSource"]
    7_4["7.4 vue: Optimistic updates"]

    T0_1["T0.1 [ТА] Playwright-проект"]
    T0_2["T0.2 [ТА] Фабрики данных"]

    1_1["1.1 usecase: User + Tariff"]
    1_2["1.2 usecase: Register + Login"]
    1_3["1.3 usecase: TOTP"]
    1_4["1.4 usecase: Recovery"]
    1_5["1.5 usecase: Logout + Session"]
    1_6["1.6 postgres: User + Tariff"]
    1_7["1.7 webapi: Auth"]
    1_8["1.8 webapi: TOTP + Recovery"]
    1_9["1.9 vue: Login + Register"]
    1_10["1.10 vue: Profile + TOTP"]
    T1_1["T1.1 [ТА] Auth тесты"]

    2_1["2.1 usecase: Project + Column"]
    2_2["2.2 postgres: Project + Column"]
    2_3["2.3 webapi: Project"]
    2_4["2.4 vue: ProjectList + Settings"]
    2_5["2.5 usecase: Board"]
    2_6["2.6 webapi: Board + Backlog + Archive"]
    2_7["2.7 vue: BoardPage layout"]
    2_8["2.8 vue: Column CRUD"]
    2_9["2.9 vue: Swimlane"]
    T2_1["T2.1 [ТА] Project/Board тесты"]

    3_1["3.1 usecase: Task (create + get)"]
    3_2["3.2 usecase: Task (update + move + archive)"]
    3_3["3.3 usecase: Comment"]
    3_4["3.4 usecase: FileAttachment"]
    3_5["3.5 postgres: Task + Comment + File"]
    3_6["3.6 webapi: Task"]
    3_7["3.7 webapi: Comment"]
    3_8["3.8 webapi: File"]
    3_9["3.9 vue: TaskCard + Create"]
    3_10["3.10 vue: DnD columns"]
    3_11["3.11 vue: TaskDetail"]
    3_12["3.12 vue: Comments + Files"]
    T3_1["T3.1 [ТА] Task тесты"]

    4_1["4.1 usecase+pg: Document"]
    4_2["4.2 webapi: Document"]
    4_3["4.3 vue: DocumentList"]
    4_4["4.4 vue: DocumentUpload"]
    4_5["4.5 MinIO интеграция"]
    T4_1["T4.1 [ТА] Document тесты"]

    5_1["5.1 usecase+pg: Group + Permission"]
    5_2["5.2 usecase+pg: Members"]
    5_3["5.3 webapi: Access"]
    5_4["5.4 webapi: PermissionCheck"]
    5_5["5.5 vue: AccessControl"]
    5_6["5.6 vue: PermissionEditor"]
    T5_1["T5.1 [ТА] Access тесты"]

    6_1["6.1 usecase+pg: Search"]
    6_2["6.2 webapi: Search"]
    6_3["6.3 vue: SearchPage"]
    6_4["6.4 usecase+pg: Reports"]
    6_5["6.5 webapi: Reports"]
    6_6["6.6 vue: ReportsPage"]
    T6_1["T6.1 [ТА] Search/Reports тесты"]

    7_1["7.1 webapi: SSE endpoint"]
    7_2["7.2 webapi: Publish events"]
    7_3["7.3 vue: EventSource"]
    7_4["7.4 vue: Optimistic updates"]
    T7_1["T7.1 [ТА] Realtime тесты"]

    8_1["8.1 Production Compose"]
    8_2["8.2 Деплой на стенд"]
    8_3["8.3 Исправление замечаний"]
    8_4["8.4 Приёмочные тесты"]

    %% Фаза 0
    0_1 --> 0_2 & 0_3 & 0_4 & 0_5 & 0_6
    0_1 --> 0_7
    0_1 --> 0_8
    0_2 --> 0_7
    0_3 --> 1_1 & 2_1 & 3_1 & 4_1 & 5_1 & 6_1 & 6_4
    0_7 --> T0_1
    0_8 --> T0_2
    T0_1 --> T0_2

    %% Фаза 1
    1_1 --> 1_2 & 1_3 & 1_4 & 1_5
    1_1 --> 1_6
    1_2 & 1_3 & 1_4 & 1_5 --> 1_7
    1_3 --> 1_8
    1_4 --> 1_8
    1_6 & 1_7 & 1_8 --> 1_9
    1_6 & 1_7 & 1_8 --> 1_10
    1_9 & 1_10 --> T1_1

    %% Фаза 2
    2_1 --> 2_2 & 2_5
    2_2 --> 2_3
    2_3 & 2_1 --> 2_4
    2_5 --> 2_6
    2_6 & 2_2 --> 2_7
    2_6 & 2_7 --> 2_8
    2_7 --> 2_9
    2_4 & 2_9 --> T2_1

    %% Фаза 3
    3_1 & 3_2 & 3_3 & 3_4 --> 3_5
    3_5 --> 3_6 & 3_7 & 3_8
    3_6 & 3_5 --> 3_9
    3_6 & 3_9 --> 3_10
    3_6 & 3_7 & 3_8 --> 3_11
    3_7 & 3_8 & 3_11 --> 3_12
    3_12 --> T3_1

    %% Фаза 3 → 2
    3_9 --> 2_7
    3_10 --> 2_7
    3_6 --> 2_6

    %% Фаза 4
    4_1 --> 4_2
    4_1 --> 4_5
    4_2 & 4_5 --> 4_3
    4_2 & 4_5 --> 4_4
    4_3 & 4_4 --> T4_1

    %% Фаза 5
    5_1 & 5_2 --> 5_3
    5_1 & 5_2 --> 5_4
    5_3 & 5_4 --> 5_5
    5_3 & 5_4 --> 5_6
    5_5 & 5_6 --> T5_1

    %% Фаза 6
    6_1 --> 6_2
    6_2 --> 6_3
    6_4 --> 6_5
    6_5 --> 6_6
    6_3 & 6_6 --> T6_1

    %% Фаза 7
    7_1 --> 7_2
    7_2 --> 7_3
    7_3 --> 7_4
    7_1 --> 0_7
    7_4 --> T7_1

    %% Фаза 8
    0_7 & 1_9 & 2_7 & 3_11 & 4_3 & 5_5 & 6_3 & 6_6 & 7_3 --> 8_1
    T1_1 & T2_1 & T3_1 & T4_1 & T5_1 & T6_1 & T7_1 --> 8_4
    8_1 --> 8_2
    8_2 --> 8_3
    8_3 --> 8_4
```

---

## Диаграмма Гантта

```mermaid
gantt
    title Kanban Board — Plan
    dateFormat  YYYY-MM-DD
    axisFormat  %d %b

    section 0. Infrastructure
    0.1 root Gradle build :t0p1, 2026-06-22, 4h
    0.2 spring entry security :t0p2, after t0p1, 4h
    0.3 usecase module domain :t0p3, after t0p1, 2h
    0.4 webapi WebFlux CORS :t0p4, after t0p1, 3h
    0.5 postgres R2DBC Flyway :t0p5, after t0p1, 3h
    0.6 nats module stub :t0p6, after t0p1, 2h
    0.7 Docker Compose dev :t0p7, after t0p2, 4h
    0.8 Vue Vite Router Pinia :t0p8, after t0p1, 4h
    T0.1 Playwright e2etest :t0p9, after t0p7, 3h
    T0.2 test data factories :t0p10, after t0p8, 3h

    section 1. Auth
    1.1 usecase User Tariff :t1p1, after t0p3, 3h
    1.2 usecase Register Login :t1p2, after t1p1, 3h
    1.3 usecase TOTP :t1p3, after t1p1, 3h
    1.4 usecase Recovery :t1p4, after t1p1, 2h
    1.5 usecase Logout Session :t1p5, after t1p1, 2h
    1.6 postgres User Tariff tables :t1p6, after t1p1, 4h
    1.7 webapi AuthController :t1p7, after t1p2, 4h
    1.8 webapi TOTP Recovery :t1p8, after t1p3, 3h
    1.9 vue Login Register :t1p9, after t1p7, 4h
    1.10 vue Profile TOTP :t1p10, after t1p7, 3h
    T1.1 Auth tests API UI :t1p11, after t1p9, 4h

    section 2. Project Board
    2.1 usecase Project Column :t2p1, after t1p1, 3h
    2.2 postgres Project Column :t2p2, after t2p1, 3h
    2.3 webapi ProjectController :t2p3, after t2p2, 3h
    2.4 vue ProjectList Settings :t2p4, after t2p3, 4h
    2.5 usecase Board query :t2p5, after t2p1, 2h
    2.6 webapi Board Backlog Archive :t2p6, after t2p2, 4h
    2.7 vue BoardPage layout :t2p7, after t2p6, 4h
    2.8 vue Column CRUD UI :t2p8, after t2p7, 3h
    2.9 vue Swimlane group :t2p9, after t2p7, 2h
    T2.1 Project Board tests API UI :t2p10, after t2p9, 4h

    section 3. Tasks
    3.1 usecase Task create get :t3p1, after t2p1, 3h
    3.2 usecase Task move archive :t3p2, after t3p1, 3h
    3.3 usecase Comment CRUD :t3p3, after t3p1, 2h
    3.4 usecase FileAttachment :t3p4, after t3p1, 3h
    3.5 postgres Task Comment File :t3p5, after t3p2, 4h
    3.6 webapi TaskController :t3p6, after t3p5, 4h
    3.7 webapi CommentController :t3p7, after t3p5, 2h
    3.8 webapi FileController :t3p8, after t3p5, 3h
    3.9+3.10 vue TaskCard DnD :t3p9, after t3p6, 4h
    3.11 vue TaskDetailPage :t3p11, after t3p6, 4h
    3.12 vue Comments Files UI :t3p12, after t3p11, 4h
    T3.1 Task tests API UI :t3p13, after t3p12, 4h

    section 4. Documents
    4.1 usecase-pg Document :t4p1, after t1p1, 3h
    4.2 webapi DocumentController :t4p2, after t4p1, 3h
    4.5 MinIO presigned integration :t4p5, after t4p1, 3h
    4.3 vue DocumentListPage :t4p3, after t4p2, 3h
    4.4 vue DocumentUpload :t4p4, after t4p2, 2h
    T4.1 Document tests API UI :t4p6, after t4p3, 3h

    section 5. Access
    5.1+5.2 usecase-pg Group Members :t5p1, after t1p1, 4h
    5.3 webapi AccessController :t5p3, after t5p1, 3h
    5.4 webapi PermissionCheck :t5p4, after t5p1, 3h
    5.5 vue AccessControlPage :t5p5, after t5p3, 3h
    5.6 vue PermissionEditor :t5p6, after t5p5, 3h
    T5.1 Access tests API UI :t5p7, after t5p6, 3h

    section 6. Search Reports
    6.1+6.2 usecase-pg-webapi Search :t6p1, after t3p5, 4h
    6.3 vue SearchPage :t6p3, after t6p1, 4h
    6.4+6.5 usecase-pg-webapi Reports :t6p4, after t3p5, 4h
    6.6 vue ReportsPage charts :t6p6, after t6p4, 4h
    T6.1 Search Reports tests API UI :t6p7, after t6p6, 4h

    section 7. Realtime
    7.1 webapi SSE endpoint Sinks :t7p1, after t0p4, 4h
    7.2 webapi Publish events :t7p2, after t7p1, 3h
    7.3 vue EventSource integration :t7p3, after t7p2, 4h
    7.4 vue Optimistic updates :t7p4, after t7p3, 3h
    T7.1 Realtime tests SSE :t7p5, after t7p4, 3h

    section 8. Finalization
    8.1 Production Docker Compose :t8p1, after t0p9 t1p11 t2p10 t3p13 t4p6 t5p7 t6p7 t7p5, 4h
    8.2 Deploy to staging :t8p2, after t8p1, 1d
    8.3 Fix review issues :t8p3, after t8p2, 1d
    8.4 Acceptance tests :t8p4, after t8p3, 1d
```

---

## Критерии готовности (Definition of Done)

Для каждой задачи:

### Backend (usecase + postgres + webapi)
- [ ] Сущности и value objects описаны в domain-пакете
- [ ] `*Operation.kt` — интерфейс с `Arg`, `Result` (sealed class Success/Failure)
- [ ] `*OperationImpl.kt` — реализация бизнес-логики
- [ ] `*Repository.kt` — интерфейс порта вывода
- [ ] Реализация порта в `postgres`-модуле (R2DBC-таблица, репозиторий)
- [ ] `*Controller.kt` / `*Handler.kt` — эндпоинты в `webapi`
- [ ] DTO-классы для запросов/ответов
- [ ] Юнит-тесты операции (`*OperationImplTest`)
- [ ] Web-тесты контроллера (`@WebFluxTest` + `WebTestClient`)
- [ ] Error-handling: все альтернативные исходы возвращают корректный HTTP-статус
- [ ] Доступ: на endpoint есть проверка прав (где требуется)
- [ ] Код проходит `ktlint` и `detekt`

### Frontend (vue)
- [ ] Компоненты страницы написаны (Composition API, `<script setup lang="ts">`)
- [ ] Pinia store — состояние и actions
- [ ] `api.ts` — вызовы к API через `request<T>()`
- [ ] Роутинг настроен
- [ ] Обработка загрузки (loading spinner/skeleton)
- [ ] Обработка ошибок (toast/notification)
- [ ] Адаптивная вёрстка (1024×768, 1920×1080, мобильные)
- [ ] Тёмная/светлая тема
- [ ] Код проходит `eslint` + `prettier`

### Тестировщик-автоматизатор
- [ ] API-тесты покрывают happy path и основные альтернативные сценарии (WebTestClient / REST-assured)
- [ ] UI-тесты (Playwright) — key user flows: навигация, CRUD основных сущностей, DnD
- [ ] Тесты не привязаны к конкретным данным (фабрики/фикстуры)
- [ ] Тесты проходят в CI (Gradle `:e2etest:check`)
- [ ] Тесты стабильны (flake-free, retry-механизм для нестабильных E2E)

### Сквозные
- [ ] API-тест (REST → БД): эндпоинт возвращает корректный ответ для happy path
- [ ] Миграция Flyway написана и применена
- [ ] Документация OpenAPI актуальна (при изменении контракта)

---

## Проверка после деплоя (checklist для этапа 8.4)

- [ ] Регистрация нового пользователя → успех
- [ ] Вход по паролю → JWT получен
- [ ] Вход по TOTP → JWT получен
- [ ] Восстановление пароля (через email) → сброс выполнен
- [ ] Создание проекта → три колонки по умолчанию
- [ ] Редактирование/удаление проекта
- [ ] Создание/редактирование/удаление колонки
- [ ] Создание задачи → отображается в бэклоге
- [ ] Drag-n-drop задачи в колонку → статус изменён
- [ ] Сортировка задач внутри колонки
- [ ] Архивирование задачи
- [ ] Комментарии: создание, редактирование, удаление
- [ ] Прикрепление файла к задаче
- [ ] Скачивание файла (presigned URL)
- [ ] Документы: загрузка, просмотр, замена, удаление
- [ ] Создание группы, добавление пользователя
- [ ] Настройка прав → проверка ограничений
- [ ] Поиск по задачам (полнотекстовый)
- [ ] CFD-диаграмма / Lead Time / Гантт
- [ ] SSE-события: задача перемещена → обновление у второго пользователя
- [ ] Лимиты тарифа: превышение → ошибка
- [ ] Адаптивная вёрстка: 1024×768, 1920×1080, мобильный
- [ ] Тёмная тема
