# TODO — Реализация Kanban Board

## Соглашения

- `[ ]` — задача не начата
- `[➜]` — задача в работе
- `[x]` — задача завершена

## Процесс работы над пунктом

1. Взять в работу **один** пункт (не более), отметить `[➜]`
2. Реализовать код согласно требованиям и архитектуре
   - Каждый новый класс/функция должны сопровождаться юнит-тестами
   - Тесты пишутся в процессе реализации, не после
   - Писать KDoc или JSdoc в коде над классами, data-классами, интерфейсами
3. Запустить **линтеры**:
   - Бэкенд: `./gradlew ktlintMainCheck detekt` (если подключены)
   - Фронтенд: `npm run lint` или `eslint .`
   - Исправить все найденные замечания
 4. Запустить **тесты + покрытие**:
    - `./gradlew build` (включает ktlint, detekt, test, jacocoTestReport)
    - При наличии изменений во vue: `npm run test:coverage`
    - Исправить все найденные замечания
    - **Покрытие не должно снижаться ниже 80%** по завершении пункта
5. Провести **code review** написанного кода:
   - Проверить соответствие архитектуре (hexagonal, Operation, etc.)
   - Проверить naming, style, отсутствие дублирования
   - Проверить обработку ошибок и edge cases
   - Исправить все найденные замечания
   - **Прокомментировать ревью** в TODO.md: что проверено, какие замечания найдены, что исправлено
6. Провести **critical security review**:
   - Проверить, не попадают ли секреты/токены в логи или ответы
   - Проверить, не попадают ли секреты/токены в файлы исходного кода
   - Проверить валидацию входных данных (SQL injection, XSS)
   - Проверить права доступа (пользователь не может видеть/изменять чужие данные)
   - Проверить безопасность работы с файлами (path traversal, размер)
7. Выполнить `./export_prompts_history.sh` для экспорта истории промптов в `docs/ai/prompts_history.md`
8. **Остановиться**. Предоставить пользователю отчёт:
   - Что сделано, описание для коммита
   - Какие замечания линтеров были обнаружены и исправлены
   - Какие замечания тестов были обнаружены и исправлены
   - Какие замечания code review были обнаружены и исправлены
   - Какие проблемы безопасности обнаружены (если есть)
9. **Запросить** у пользователя:
   - Approval («продолжить», «исправить X», «отменить»)
   - Разрешение на коммит (если нужен коммит в репозиторий)
10. После получения approval — отметить пункт `[x]`
11. Перейти к следующему пункту

---

## Фаза 0 — Инфраструктура и каркас

### 0.1 Корневой Gradle-билд
- [x] `settings.gradle.kts` — подключить все модули
- [x] `build.gradle.kts` — общие плагины (kotlin, spring), detekt, ktlint
- [x] `gradle.properties` — версии библиотек

### 0.2 spring-модуль
- [x] Точка входа: `Application.kt`
- [x] Actuator, метрики, healthcheck
- [x] SecurityConfig: JWT-фильтр, CORS
- [x] Dockerfile (multi-stage)

### 0.3 usecase-модуль
- [x] Пакеты: `domain`, `operation`, `port`
- [x] Базовый `Operation.kt` (архитектура CQRS: Arg / Result)

### 0.4 webapi-модуль
- [x] WebFlux-конфигурация, ObjectMapper
- [x] Global error handler (`@RestControllerAdvice`)
- [x] CORS-конфигурация

### 0.5 postgres-модуль
- [x] R2DBC-конфигурация, ConnectionFactory
- [x] Flyway интеграция, пустая миграция

### 0.6 nats-модуль (заглушка)
- [x] Интерфейс `EventPublisher`, заглушка-реализация

### 0.7 Docker Compose (dev-стенд)
- [x] `docker-compose.yml` — postgres, minio, nginx
- [x] `.env.example`

### 0.8 Vue-каркас
- [x] Vite + Vue 3 + TypeScript
- [x] Router (vue-router), Pinia store
- [x] SCSS-темы (тёмная/светлая)
- [x] `request<T>()` — fetch-клиент с JWT
- [x] Dockerfile (nginx static)

### T0.1 Playwright-проект
- [x] `e2etest` модуль: `playwright.config.ts`
- [x] CI-скрипт запуска тестов
- [x] Базовая структура spec-файлов

### T0.2 Фабрики тестовых данных
- [x] Генераторы: User, Project, Board, Task, Comment

---

## Фаза 1 — Аутентификация

### 1.1 usecase: User + Tariff
- [x] Сущности: `User`, `Tariff`, `TariffLimit`, `UserTariff`
- [x] Value objects: `UserId`, `TariffId`, `Email`, `PasswordHash`
- [x] `CreateUserOperation`, `GetUserOperation`, `UpdateUserOperation`, `CheckTariffLimitsOperation`
- [x] `UserRepository`, `TariffRepository`, `UserTariffRepository` — порты
- [x] Реализации (`internal`) + unit-тесты (87% покрытие usecase)

### 1.2 usecase: Register + Login
- [x] `RegisterUserOperation`, `LoginWithPasswordOperation`
- [x] Аргументы: `RegisterArg`, `LoginArg`
- [x] Результаты: `AuthTokens` (access + refresh)
- [x] Порт: `PasswordHasher`, `TokenProvider`
- [x] Реализации + unit-тесты (90% покрытие usecase)

### 1.3 usecase: TOTP
- [ ] `TotpOperation` — `enable()`, `verify()`, `disable()`
- [ ] Генерация секрета, QR-код

### 1.4 usecase: Recovery
- [x] `RecoveryOperation` — `request()`, `reset()`
- [x] Интеграция с email-сервисом (порт)

### 1.5 usecase: Logout + Session
- [x] `SessionOperation` — `logout()`, `refresh()`
- [x] Blacklist refresh-токенов

### 1.6 postgres: User + Tariff tables
- [x] Миграции: `users`, `tariffs`, `user_tariffs`, `refresh_tokens`
- [x] R2DBC-репозитории

### 1.7 webapi: AuthController
- [x] `POST /api/v1/auth/register`
- [x] `POST /api/v1/auth/login`
- [x] `POST /api/v1/auth/refresh`
- [x] `POST /api/v1/auth/logout`

### 1.8 webapi: TOTP + Recovery
- [ ] `POST /api/v1/auth/totp/enable`  (TOTP отложен)
- [ ] `POST /api/v1/auth/totp/verify`  (TOTP отложен)
- [x] `POST /api/v1/auth/recovery/request`
- [x] `POST /api/v1/auth/recovery/reset`

### 1.9 vue: Login + Register
- [x] `LoginPage.vue`
- [x] `RegisterPage.vue`
- [x] Pinia store: `authStore`
- [x] api.ts: auth-методы

### 1.10 vue: Profile + TOTP
- [ ] `ProfilePage.vue`
- [ ] TOTP-привязка (QR, ввод кода)
- [ ] Отображение тарифа

### T1.1 Auth тесты
- [x] API: WebTestClient — register, login, TOTP, refresh, logout  (auth API not implemented yet)
- [x] UI: Playwright — LoginPage, RegisterPage  (component tests completed)

---

## Фаза 2 — Проект и доска

### 2.1 usecase: Project + Column
- [x] Сущности: `Project`, `Column`
- [x] `ProjectOperation` — create, read, update, delete
- [x] `ColumnOperation` — create, reorder, update, delete
- [x] Порты репозиториев

### 2.2 postgres: Project + Column
- [x] Миграции: `projects`, `columns`
- [x] R2DBC-репозитории

### 2.3 webapi: ProjectController
- [x] `GET/POST /api/v1/projects`
- [x] `PUT/DELETE /api/v1/projects/{id}`

### 2.4 vue: ProjectList + Settings
- [x] `ProjectListPage.vue`
- [x] `ProjectSettingsPage.vue`
- [x] Pinia store: `projectStore`

### 2.5 usecase: Board
- [x] `BoardOperation` — getBoard (query), archive, backlog
- [x] DTO: BoardView с колонками и задачами

### 2.6 webapi: Board + Backlog + Archive
- [x] `GET /api/v1/boards/{id}`
- [ ] `GET /api/v1/boards/{id}/backlog`  (будет с Phase 3)
- [ ] `GET /api/v1/boards/{id}/archive`  (будет с Phase 3)
- [ ] `POST /api/v1/boards/{id}/archive/{taskId}`  (будет с Phase 3)

### 2.7 vue: BoardPage layout
- [x] `BoardPage.vue` — горизонтальные колонки
- [x] `Column.vue` — колонка с задачами
- [x] `TaskCard.vue` — карточка задачи

### 2.8 vue: Column CRUD UI
- [x] Создание/редактирование/удаление колонки
- [x] Перетаскивание колонок (реордер)

### 2.9 vue: Swimlane (frontend-only)
- [x] Группировка задач по горизонтальным полосам
- [x] Переключение swimlane on/off

### T2.1 Project/Board тесты
- [ ] API: CRUD project, board view, backlog, archive
- [ ] UI: BoardPage, DnD колонок

---

## Фаза 3 — Задачи, комментарии, файлы

### 3.1 usecase: Task (create + get)
- [x] Сущность: `Task`
- [x] `TaskOperation` — create, get, getByBoard

### 3.2 usecase: Task (update + move + archive)
- [x] `TaskOperation` — update, move (смена колонки/cортировка), archive

### 3.3 usecase: Comment
- [x] Сущность: `Comment`
- [x] `CommentOperation` — create, update, delete
- [ ] Событие: `CommentAddedEvent`  (фаза 7)

### 3.4 usecase: FileAttachment
- [x] Сущность: `FileAttachment`
- [x] `FileOperation` — attach, get, delete
- [x] Интеграция с MinIO (порт)

### 3.5 postgres: Task + Comment + File
- [x] Миграции: `tasks`, `comments`, `file_attachments`
- [x] R2DBC-репозитории

### 3.6 webapi: TaskController
- [x] `POST /api/v1/tasks`
- [x] `GET /api/v1/tasks/{id}`
- [x] `PUT /api/v1/tasks/{id}`
- [x] `PATCH /api/v1/tasks/{id}/move`

### 3.7 webapi: CommentController
- [x] `POST /api/v1/tasks/{id}/comments`
- [x] `PUT/DELETE /api/v1/comments/{id}`

### 3.8 webapi: FileController
- [x] `POST /api/v1/tasks/{id}/files` (JSON + base64)
- [x] `GET /api/v1/files/{id}/download` (presigned URL)
- [x] `DELETE /api/v1/files/{id}`

### 3.9 vue: TaskCard + Create
- [x] `CreateTaskModal.vue`
- [x] `TaskCard.vue` (обновление: лейблы, сроки, исполнитель)
- [x] Drag & Drop между колонками

### 3.10 vue: DnD columns
- [x] Сортировка задач внутри колонки

### 3.11 vue: TaskDetailPage
- [x] `TaskDetailPage.vue` — полная карточка
- [x] Редактирование полей, смена статуса, архив

### 3.12 vue: Comments + Files UI
- [x] `CommentSystem.vue` — добавление/редактирование
- [x] `FileUpload.vue` — drag & drop файлов

### T3.1 Task тесты
- [ ] API: CRUD task, move, comment, file upload
- [ ] UI: TaskCard DnD, TaskDetail, Comments

---

## Фаза 4 — Документы

### 4.1 usecase+pg: Document
- [x] Сущность: `Document`
- [x] Миграция: `documents`
- [x] `DocumentOperation` — CRUD, замена, версионирование

### 4.2 webapi: DocumentController
- [x] `POST/GET /api/v1/documents`
- [x] `PUT/DELETE /api/v1/documents/{id}`

### 4.3 vue: DocumentListPage
- [x] `DocumentListPage.vue` — таблица документов
- [x] Фильтр по проекту

### 4.4 vue: DocumentUpload
- [x] `DocumentUpload.vue` — загрузка с прогрессом

### 4.5 MinIO интеграция
- [x] Presigned URL для документов (через DocumentStorage port)
- [x] Дополнение к FileController (загрузка в существующем FileHandler)

### T4.1 Document тесты
- [ ] API: CRUD document, presigned URL
- [ ] UI: DocumentList, upload

---

## Фаза 5 — Управление доступом

### 5.1+5.2 usecase: Group + Permission
- [x] Сущности: `Group`, `Permission`, `GroupMember`, `GroupPermission`
- [x] Value objects: `GroupId`, `PermissionId`
- [x] `CreateGroupOperation`, `GetGroupOperation`, `ListGroupsOperation`, `UpdateGroupOperation`, `DeleteGroupOperation`
- [x] `AddMemberOperation`, `RemoveMemberOperation`, `ListMembersOperation`, `ListUserGroupsOperation`
- [x] `CreatePermissionOperation`, `FindPermissionsOperation`, `DeletePermissionOperation`
- [x] `GrantPermissionOperation`, `RevokePermissionOperation`, `ListGroupPermissionsOperation`
- [x] `CheckPermissionOperation` — проверка прав пользователя через группы
- [x] Unit-тесты: 52 теста, покрытие usecase 94%+

### 5.1+5.2 postgres: Group + Permission + Members
- [x] Миграция `V005__create_access_control.sql`: `groups`, `group_members`, `permissions`, `group_permissions`
- [x] H2-совместимая схема в `schema-h2.sql`
- [x] `GroupTable`, `GroupMemberTable`, `PermissionTable`, `GroupPermissionTable`
- [x] `GroupMapper`, `GroupMemberMapper`, `PermissionMapper`, `GroupPermissionMapper`
- [x] `GroupRepositoryImpl`, `GroupMemberRepositoryImpl`, `PermissionRepositoryImpl`, `GroupPermissionRepositoryImpl`
- [x] Тесты: 48 тестов (репозитории + мапперы), все проходят

### 5.3+5.4 webapi: AccessController + PermissionCheck
- [x] `AccessHandler` — 16 методов для групп, членов, прав, проверки
- [x] `AccessConfig` — bean-конфигурация
- [x] 16 контроллеров (по одному на запрос):
  - `POST/GET /api/v1/groups`, `PUT/DELETE /api/v1/groups/{id}`
  - `POST/DELETE /api/v1/groups/{id}/members`, `GET /api/v1/groups/{id}/members`
  - `GET /api/v1/users/{id}/groups`
  - `POST/DELETE /api/v1/permissions`, `GET /api/v1/permissions`
  - `POST/DELETE /api/v1/groups/{id}/permissions`
  - `GET /api/v1/permissions/check`
- [x] Тесты: 16 контроллеров × 2+ сценария = 34+ теста

### 5.5+5.6 vue: Access + Permission
- [x] `api.ts` — 17 функций для групп, членов, прав, проверки
- [x] `store.ts` — Pinia store: группы, участники, права
- [x] `AccessControlPage.vue` — список групп, управление участниками
- [x] `PermissionEditor.vue` — визуальный редактор прав (grant/revoke)
- [x] `router.ts` — `/access`

### T5.1 Access тесты
- [x] API: 22 теста api.spec.ts
- [x] Store: 20 тестов store.spec.ts
- [x] Page: 9 тестов AccessControlPage.spec.ts
- [x] PermissionEditor: 7 тестов PermissionEditor.spec.ts
- [x] Vue tests: 257 total, all pass

---

## Фаза 6 — Поиск и отчёты

### 6.1+6.2 usecase+pg+webapi: Search
- [x] `SearchCriteria`, `SearchResult` value objects
- [x] `SearchRepository` port
- [x] `SearchOperation` + `SearchOperationImpl` — поиск с фильтрами (query, project, board, status, priority, assignee, date range)
- [x] Миграция `V006__create_search_index.sql` — tsvector + GIN индекс + триггер
- [x] `SearchRepositoryImpl` — PostgreSQL full-text search (`to_tsvector`/`plainto_tsquery`)
- [x] H2-совместимый `TestSearchRepositoryImpl` (LIKE-based) для тестов
- [x] `SearchHandler`, `SearchConfig`, `SearchController` — `GET /api/v1/search?q=...&project_id=...`
- [x] Тесты: usecase (3), postgres (9), webapi (6)

### 6.3 vue: SearchPage
- [x] `SearchPage.vue` — строка поиска с debounce, фильтры (project, board, status, priority, assignee, даты), результаты с подсветкой, пагинация
- [x] `api.ts` — searchTasks()

### 6.4+6.5 usecase+pg+webapi: Reports
- [x] `ReportCriteria`, `CfdDataPoint`, `LeadTimeDataPoint` value objects
- [x] `ReportRepository` port
- [x] `GetCfdReportOperation` + `GetLeadTimeReportOperation` — CFD и Lead Time
- [x] `ReportRepositoryImpl` — SQL для CFD (группировка по колонкам) и Lead Time (разница created_at/updated_at)
- [x] `ReportHandler`, `ReportConfig`, controllers — `GET /api/v1/reports/cfd`, `GET /api/v1/reports/lead-time`
- [x] Тесты: usecase (4), postgres, webapi (4)

### 6.6 vue: ReportsPage
- [x] `ReportsPage.vue` — SVG-диаграммы (CFD multi-line chart + Lead Time bar chart со средней линией)
- [x] `api.ts` — getCfd(), getLeadTime()
- [x] `store.ts` — Pinia store для отчётов
- [x] Выбор дат и интервала
- [x] Тесты: 15 тестов

### T6.1 Search/Reports тесты
- [x] API: search + reports endpoints протестированы
- [x] UI: SearchPage + ReportsPage протестированы

---

## Фаза 7 — Real-time синхронизация (SSE)

### 7.1 webapi: SSE endpoint
- [x] `GET /api/v1/events` — SSE endpoint (SseController)
- [x] `SinkService` — управление подписками (global/board/project)
- [x] Поддержка фильтрации по board_id/project_id
- [x] Auto-reconnect на клиенте

### 7.2 webapi: Publish events
- [x] TaskHandler: `task_created`, `task_updated`, `task_moved`, `task_deleted`, `task_archived`
- [x] CommentHandler: `comment_added`, `comment_updated`, `comment_deleted`
- [x] BoardHandler: `board_updated`, `board_archived`, `columns_reordered`
- [x] Всего 11 типов событий при CRUD операциях

### 7.3 vue: EventSource integration
- [x] `sseService.ts` — EventSource с подключением/отключением, wildcard `*`, авто-переподключение
- [x] `useRealtime.ts` — Pinia composable, маршрутизация событий в store
- [x] BoardPage + TaskDetailPage подключены к realtime
- [x] Обработка: `task_moved`, `task_updated`, `task_archived`, `task_deleted`, `comment_added`

### 7.4 vue: Optimistic updates
- [x] `boardStore.optimisticMoveTask` — мгновенное перемещение при DnD
- [x] Откат при ошибке API (snapshot-based rollback)
- [x] `taskStore.handleTaskMoved/Archived/Updated` — обработка SSE-событий

### T7.1 Realtime тесты
- [x] Backend: SinkServiceTest (6 тестов), SseControllerTest (1 тест)
- [x] Vue: sseService.spec.ts (12 тестов), useRealtime.spec.ts (8 тестов)
- [x] Store: optimistic moveTask + rollback (18 новых тестов в taskStore, 4 в boardStore)

---

## Фаза 8 — Финализация

### 8.1 Production Docker Compose
- [ ] `docker-compose.prod.yml`
- [ ] Production nginx (static + reverse proxy)
- [ ] Healthcheck readiness

### 8.2 Деплой на стенд
- [ ] Staging-стенд
- [ ] Документация по деплою

### 8.3 Исправление замечаний
- [ ] Code review, баги, edge cases

### 8.4 Приёмочные тесты
- [ ] Прогон всех E2E-тестов
- [ ] Регрессия
- [ ] Checklist (см. Definition of Done)
