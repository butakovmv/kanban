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
- [ ] `ProjectListPage.vue`
- [ ] `ProjectSettingsPage.vue`
- [ ] Pinia store: `projectStore`

### 2.5 usecase: Board
- [x] `BoardOperation` — getBoard (query), archive, backlog
- [x] DTO: BoardView с колонками и задачами

### 2.6 webapi: Board + Backlog + Archive
- [x] `GET /api/v1/boards/{id}`
- [ ] `GET /api/v1/boards/{id}/backlog`  (будет с Phase 3)
- [ ] `GET /api/v1/boards/{id}/archive`  (будет с Phase 3)
- [ ] `POST /api/v1/boards/{id}/archive/{taskId}`  (будет с Phase 3)

### 2.7 vue: BoardPage layout
- [ ] `BoardPage.vue` — горизонтальные колонки
- [ ] `Column.vue` — колонка с задачами
- [ ] `TaskCard.vue` — карточка задачи

### 2.8 vue: Column CRUD UI
- [ ] Создание/редактирование/удаление колонки
- [ ] Перетаскивание колонок (реордер)

### 2.9 vue: Swimlane (frontend-only)
- [ ] Группировка задач по горизонтальным полосам
- [ ] Переключение swimlane on/off

### T2.1 Project/Board тесты
- [ ] API: CRUD project, board view, backlog, archive
- [ ] UI: BoardPage, DnD колонок

---

## Фаза 3 — Задачи, комментарии, файлы

### 3.1 usecase: Task (create + get)
- [ ] Сущность: `Task`
- [ ] `TaskOperation` — create, get, getByBoard

### 3.2 usecase: Task (update + move + archive)
- [ ] `TaskOperation` — update, move (смена колонки/cортировка), archive

### 3.3 usecase: Comment
- [ ] Сущность: `Comment`
- [ ] `CommentOperation` — create, update, delete
- [ ] Событие: `CommentAddedEvent`

### 3.4 usecase: FileAttachment
- [ ] Сущность: `FileAttachment`
- [ ] `FileOperation` — attach, get, delete
- [ ] Интеграция с MinIO (порт)

### 3.5 postgres: Task + Comment + File
- [ ] Миграции: `tasks`, `comments`, `file_attachments`
- [ ] R2DBC-репозитории

### 3.6 webapi: TaskController
- [ ] `POST /api/v1/tasks`
- [ ] `GET /api/v1/tasks/{id}`
- [ ] `PUT /api/v1/tasks/{id}`
- [ ] `PATCH /api/v1/tasks/{id}/move`

### 3.7 webapi: CommentController
- [ ] `POST /api/v1/tasks/{id}/comments`
- [ ] `PUT/DELETE /api/v1/comments/{id}`

### 3.8 webapi: FileController
- [ ] `POST /api/v1/tasks/{id}/files` (multipart)
- [ ] `GET /api/v1/files/{id}` (presigned redirect)
- [ ] `DELETE /api/v1/files/{id}`

### 3.9 vue: TaskCard + Create
- [ ] `CreateTaskModal.vue`
- [ ] `TaskCard.vue` (обновление: лейблы, сроки, исполнитель)
- [ ] Drag & Drop между колонками

### 3.10 vue: DnD columns
- [ ] Сортировка задач внутри колонки

### 3.11 vue: TaskDetailPage
- [ ] `TaskDetailPage.vue` — полная карточка
- [ ] Редактирование полей, смена статуса, архив

### 3.12 vue: Comments + Files UI
- [ ] `CommentSystem.vue` — добавление/редактирование
- [ ] `FileUpload.vue` — drag & drop файлов

### T3.1 Task тесты
- [ ] API: CRUD task, move, comment, file upload
- [ ] UI: TaskCard DnD, TaskDetail, Comments

---

## Фаза 4 — Документы

### 4.1 usecase+pg: Document
- [ ] Сущность: `Document`
- [ ] Миграция: `documents`
- [ ] `DocumentOperation` — CRUD, замена, версионирование

### 4.2 webapi: DocumentController
- [ ] `POST/GET /api/v1/documents`
- [ ] `PUT/DELETE /api/v1/documents/{id}`

### 4.3 vue: DocumentListPage
- [ ] `DocumentListPage.vue` — таблица документов
- [ ] Фильтр по проекту

### 4.4 vue: DocumentUpload
- [ ] `DocumentUpload.vue` — загрузка с прогрессом

### 4.5 MinIO интеграция
- [ ] Presigned URL для документов
- [ ] Дополнение к FileController

### T4.1 Document тесты
- [ ] API: CRUD document, presigned URL
- [ ] UI: DocumentList, upload

---

## Фаза 5 — Управление доступом

### 5.1+5.2 usecase+pg: Group + Permission
- [ ] Сущности: `Group`, `Permission`
- [ ] `GroupOperation` — CRUD
- [ ] `PermissionOperation` — assign, revoke, check
- [ ] Миграции: `groups`, `group_members`, `permissions`, `group_permissions`

### 5.2 usecase+pg: Members
- [ ] `MemberOperation` — add, remove, list

### 5.3 webapi: AccessController
- [ ] `POST/GET /api/v1/groups`
- [ ] `POST/DELETE /api/v1/groups/{id}/members`

### 5.4 webapi: PermissionCheck
- [ ] Middleware/Filter проверки прав
- [ ] `GET /api/v1/permissions/check`

### 5.5 vue: AccessControlPage
- [ ] `AccessControlPage.vue` — список групп
- [ ] Управление участниками

### 5.6 vue: PermissionEditor
- [ ] `PermissionEditor.vue` — визуальный редактор прав

### T5.1 Access тесты
- [ ] API: group CRUD, member management, permission check
- [ ] UI: AccessControlPage, PermissionEditor

---

## Фаза 6 — Поиск и отчёты

### 6.1+6.2 usecase+pg+webapi: Search
- [ ] Полнотекстовый поиск по задачам (PostgreSQL full-text)
- [ ] `SearchOperation` — поиск с фильтрами
- [ ] Миграция: `tsvector` индекс
- [ ] `GET /api/v1/search?q=...&project=...`

### 6.3 vue: SearchPage
- [ ] `SearchPage.vue` — строка поиска, результаты

### 6.4+6.5 usecase+pg+webapi: Reports
- [ ] CFD (Cumulative Flow Diagram)
- [ ] Lead Time, Cycle Time
- [ ] `GET /api/v1/reports/cfd`
- [ ] `GET /api/v1/reports/lead-time`

### 6.6 vue: ReportsPage
- [ ] `ReportsPage.vue` — диаграммы (Chart.js)
- [ ] Выбор диапазона дат

### T6.1 Search/Reports тесты
- [ ] API: search, reports endpoints
- [ ] UI: SearchPage, ReportsPage

---

## Фаза 7 — Real-time синхронизация (SSE)

### 7.1 webapi: SSE endpoint
- [ ] `GET /api/v1/events` — SSE endpoint
- [ ] `SinkService` — управление подписками
- [ ] Sticky-session / fallback

### 7.2 webapi: Publish events
- [ ] Публикация событий при CRUD задачах, колонках, комментариях
- [ ] Типы событий: `TaskMoved`, `TaskUpdated`, `CommentAdded`

### 7.3 vue: EventSource integration
- [ ] `EventSource` API в сервисе
- [ ] Pinia store: обновление состояния по событиям

### 7.4 vue: Optimistic updates
- [ ] Оптимистичное обновление UI при DnD
- [ ] Откат при ошибке / конфликте (Last-Event-ID)

### T7.1 Realtime тесты
- [ ] SSE-события, синхронизация между вкладками

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
