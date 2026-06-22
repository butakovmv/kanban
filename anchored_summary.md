# Anchored Summary

## Goal
Отладить e2e тесты — исправить цепочку регистрации→создания проекта→доски→задачи→комментария и связанные с ней баги на бэкенде и фронтенде.

## Constraints & Preferences
- Фронтенд `api.ts` экспортирует camelCase‑типы; маппинг raw‑ответа лежит в том же файле
- Pinia store использует camelCase; localStorage сохраняет токены и user
- Новые пользователи автоматически получают тариф Free ("No active tariff" не должен блокировать создание проекта)
- Все исправления должны быть самодостаточными (без Flyway‑миграции, которая никогда не накатывалась)

## Progress
### Done
- **Auth raw‑типы и маппинг** — `RawAuthTokens`, `RawAuthUser`, `RawAuthResponse` (snake_case) + `toAuthResponse`/`toAuthTokens` в `api.ts`
- **localStorage persistence** — `persistSession()`, `clearPersistedSession()`, `restoreSession()` в `store.ts`; токены восстанавливаются при перезагрузке страницы
- **Vue unit tests все проходят (324/324)**
- **Назначение тарифа Free при регистрации** — `RegisterUserOperationImpl` создаёт “Free” тариф и `UserTariff` для нового пользователя
- **TariffRepository.save()** — добавлен в интерфейс и реализован в `TariffRepositoryImpl` (Postgres, INSERT ON CONFLICT DO UPDATE)
- **Jackson JavaTimeModule** — зарегистрирован в `WebConfig.kt`; `WRITE_DATES_AS_TIMESTAMPS` выключен (ISO‑строки)
- **Project creation 500 “Instant”** — исправлен; теперь возвращает валидный snake_case JSON с датами
- **E2E helpers** — `login()` читает `access_token`; `createBoardViaApi()` парсит `body.board.id`; удалён `createColumnViaApi`
- **`toTask()` / `toComment()` / `toFile()` — snake_case маппинг** — добавлены `RawTask`, `RawComment`, `RawFileAttachment`; `toTask` читает `board_id` вместо `boardId`; добавлен `toDate()` для числовых timestamp’ов
- **Создание комментария — отсутствовал `author_id`** — добавлен `authorId` в `CreateCommentRequest`, передан в API; `CommentSystem.vue` читает `authStore.user.id`
- **`fetch.ts` — обработка 204 No Content** — `request()` теперь возвращает `undefined` при статусе 204, не пытаясь парсить пустое тело
- **Docker‑билд починен** — `LoggingWebFilter.kt`: обратные кавычки `` `$method $path` `` → `"$method $path"`
- **E2E проект тесты** — обновление имени проверяет `input[value=…]` вместо текста; удаление проекта мокает `window.confirm`
- **E2E auth тесты** — исправлен strict‑mode (дубли ссылок в nav и форме); логин создаёт пользователя через UI
- **E2E селекторы документов/файлов** — `getByText(/documents|files/i)` → `getByRole('heading', …)`
- **Document upload — селектор Upload в модалке** — `page.getByRole('dialog').getByRole('button')` вместо `page.getByRole('button')`
- **Document delete — confirm** — `page.evaluate(() => window.confirm = () => true)`
- **Backend `DocumentRepositoryImpl` — `uploaded_by` UUID** — `.bind("uploadedBy", UUID.fromString(...))` вместо строки (Postgres требовал UUID, падало с 42804)
- **Search frontend URL** — `GET /search` вместо `GET /tasks/search` (бэкенд ожидает `/api/v1/search`, а не `/api/v1/tasks/search`)
- **Search test strict‑mode** — `getByText('Filters', { exact: true })` вместо `getByText(/filters/i)`

### In Progress
- **E2E smoke‑test результаты: 12/13 проходят** — падает только `realtime.spec.ts` (два разных пользователя, нет прав доступа к доске второго)
- **Полный прогон e2e: 46/51 проходят** (было 24/51)

### Not Fixed (5 remaining failures)
- **realtime.spec.ts** — SSE sync cross‑user: `rt2` не имеет доступа к доске `rt1`; нужна фича коллаборации/шаринга
- **access.spec.ts:42** — "add member to group": API endpoint для групп может отсутствовать
- **file.spec.ts:60,76** — upload/delete file: компонент `FileUpload.vue` заглушка ("Real upload to MinIO is not yet implemented")
- **report.spec.ts:70** — "should show loading state when loading CFD": возможно не хватает эндпоинта или тест проверяет несуществующее состояние

## Key Decisions
- **Всё то же, что в предыдущей версии** + `DocumentRepositoryImpl` кастует `uploadedBy` в UUID через `UUID.fromString()`
- **Search URL** — изменил `/tasks/search` на `/search` во фронтенде (бэкенд контроллер на `/api/v1/search`)

## Relevant Files (new/changed)
- `postgres/src/main/kotlin/com/kanban/postgres/document/DocumentRepositoryImpl.kt` — `UUID.fromString()` для `uploaded_by`
- `vue/src/module/search/api.ts` — URL изменён с `/tasks/search` на `/search`
- `vue/src/module/search/__tests__/api.spec.ts` — обновлены ожидания URL
- `e2etest/specs/document.spec.ts` — диалоговый селектор Upload, confirm mock
- `e2etest/specs/search.spec.ts` — строгий селектор Filters
