# Контракт: PostgreSQL

## Обзор
Реляционная база данных — единственное stateful хранилище внутри системы. Хранит все данные приложения: пользователей, проекты, колонки, задачи, комментарии, метаданные файлов, группы, права доступа, тарифы, события (лог).

## Протокол
- **Протокол:** R2DBC (Reactive Relational Database Connectivity) over TCP
- **Драйвер:** `r2dbc-postgresql`
- **Порт:** 5432 (стандартный PostgreSQL)
- **Транспорт:** TCP
- **Аутентификация:** username/password (SCRAM-SHA-256)

## Поток вызова

```
┌──────────────┐    R2DBC TCP :5432    ┌──────────────┐
│ API Container│ ───────────────────→  │  PostgreSQL  │
│  (WebFlux)  │ ←───────────────────  │              │
│             │   Flux<T> / Mono<T>    │              │
└──────────────┘                        └──────────────┘
```

Все запросы — реактивные (`Mono`/`Flux`). Пул соединений — R2DBC Pool.

## Схемы и таблицы

### Пользователи и аутентификация

| Таблица | Назначение | Связи |
|---|---|---|
| `users` | Учётные записи пользователей | `tariff_id → tariffs.id` |
| `tariffs` | Тарифные планы | — |

**`users`:**
- `id UUID PRIMARY KEY`
- `login VARCHAR(50) UNIQUE NOT NULL` — латиница/цифры/`_`
- `password_hash VARCHAR(60) NOT NULL` — bcrypt
- `email VARCHAR(254) UNIQUE NOT NULL`
- `totp_secret VARCHAR(32)` — nullable, base32
- `tariff_id UUID NOT NULL REFERENCES tariffs(id)`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

**`tariffs`:**
- `id UUID PRIMARY KEY`
- `name VARCHAR(50) UNIQUE NOT NULL`
- `max_projects INT NOT NULL DEFAULT 2`
- `max_columns INT NOT NULL DEFAULT 10`
- `max_assignees INT NOT NULL DEFAULT 5`

### Проекты и доска

| Таблица | Назначение | Связи |
|---|---|---|
| `projects` | Проекты | `owner_id → users.id` |
| `columns` | Колонки доски | `project_id → projects.id` |

**`projects`:**
- `id UUID PRIMARY KEY`
- `owner_id UUID NOT NULL REFERENCES users(id)`
- `name VARCHAR(200) NOT NULL`
- `description VARCHAR(2000)`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

**`columns`:**
- `id UUID PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE`
- `name VARCHAR(200) NOT NULL`
- `sort_order INT NOT NULL`
- `task_ids UUID[] NOT NULL DEFAULT '{}'` — упорядоченный список задач

### Задачи

| Таблица | Назначение | Связи |
|---|---|---|
| `tasks` | Задачи проекта | `project_id → projects.id`, `assignee_id → users.id` |

**`tasks`:**
- `id UUID PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE`
- `title VARCHAR(500) NOT NULL`
- `description VARCHAR(10000)`
- `use_case VARCHAR(200)` — nullable (для свимлайнов frontend)
- `priority VARCHAR(10) NOT NULL CHECK (priority IN ('low','medium','high','critical'))`
- `status VARCHAR(36) NOT NULL` — колонка: UUID колонки, 'backlog' или 'archive'
- `labels VARCHAR(50)[] NOT NULL DEFAULT '{}'` — метки (макс. 10)
- `assignee_id UUID REFERENCES users(id)` — nullable
- `work_start_date TIMESTAMPTZ`
- `archived_at TIMESTAMPTZ`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

### Комментарии

**`comments`:**
- `id UUID PRIMARY KEY`
- `task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE`
- `author_id UUID NOT NULL REFERENCES users(id)`
- `text VARCHAR(5000) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ`

### Файлы

**`files`:**
- `id UUID PRIMARY KEY`
- `task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE`
- `name VARCHAR(255) NOT NULL`
- `size BIGINT NOT NULL CHECK (size <= 10485760)` — 10 MB max
- `storage_key VARCHAR(500) NOT NULL`
- `uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

### Документы

**`documents`:**
- `id UUID PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE`
- `type VARCHAR(50) NOT NULL` — enum (14 типов)
- `name VARCHAR(200) NOT NULL`
- `storage_key VARCHAR(500) NOT NULL`
- `size BIGINT NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `UNIQUE(project_id, type)`

### Группы и права доступа

| Таблица | Назначение | Связи |
|---|---|---|
| `groups` | Группы пользователей проекта | `project_id → projects.id` |
| `group_members` | Участники групп | `group_id → groups.id`, `user_id → users.id` |

**`groups`:**
- `id UUID PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE`
- `name VARCHAR(100) NOT NULL`
- `permissions JSONB NOT NULL` — `{ "task:read": bool, "task:create": bool, ... }`

**`group_members`:**
- `group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `PRIMARY KEY (group_id, user_id)`

### Лог событий

**`events`:**
- `id BIGSERIAL PRIMARY KEY`
- `project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE`
- `type VARCHAR(30) NOT NULL` — `TASK_CREATED`, `TASK_MOVED` и т.д.
- `payload JSONB NOT NULL`
- `user_id UUID NOT NULL REFERENCES users(id)`
- `timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()`

**Индексы:**
- `idx_events_project_timestamp ON events(project_id, timestamp DESC)`

### Токены восстановления

**`recovery_tokens`:**
- `token UUID PRIMARY KEY`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `expires_at TIMESTAMPTZ NOT NULL`
- `used BOOLEAN NOT NULL DEFAULT FALSE`

**Правила:**
- TTL 1 час; при сбросе пароля токен помечается `used = TRUE`

## Операции (группы запросов)

| Тип | Примеры | Частота |
|---|---|---|
| Auth | INSERT users, SELECT user by login, INSERT/UPDATE recovery_tokens | Низкая |
| CRUD проекты | INSERT/SELECT/UPDATE/DELETE projects, columns | Средняя |
| CRUD задачи | INSERT/SELECT/UPDATE tasks, comments, files | Высокая |
| Доступ | SELECT groups, group_members, проверка прав | Средняя |
| Отчёты | Агрегация задач по статусам, Lead Time, лог событий | Низкая |
| Поиск | Full-text search (pg_trgm или `to_tsvector`) | Средняя |
| Админ | SELECT users, UPDATE tariffs | Очень низкая |

## Параметры соединения

| Параметр | Значение | Примечание |
|---|---|---|
| Пул соединений | 10-30 (настраивается) | `initialSize=10, maxSize=30` |
| Таймаут соединения | 5 секунд | `connectionTimeout` |
| SSL | Предпочтительно (disable в dev) | `sslMode=require` / `prefer` |
| idle timeout | 10 минут | Закрытие неиспользуемых соединений |
| max lifetime | 30 минут | Принудительное пересоздание соединения |

## Обработка ошибок

| Ситуация | Действие сервера |
|---|---|
| `Connection refused` (БД не запущена) | Application fail fast при старте; R2DBC `retry` 3 раза с backoff |
| `Deadlock detected` (40P01) | PostgreSQL сам разрешает; клиент получает ошибку → retry на уровне use case |
| `Unique violation` (23505) | Возвращается `409 Conflict` (логин занят) |
| `Foreign key violation` (23503) | `404 Not Found` или `422` |
| `Check constraint` (23514) | `400 Bad Request` |
| Serialization failure (40001) | Retry на уровне use case (SSI) |

## Миграции
- Инструмент: Flyway (реактивный) или Liquibase
- Миграции — часть модуля `postgres`
- Именование: `V<номер>__<описание>.sql`

## Конфигурация (application.yml)

```yaml
spring:
  r2dbc:
    url: "r2dbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:kanban}"
    username: "${DB_USER:kanban}"
    password: "${DB_PASSWORD}"
  flyway:
    url: "jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:kanban}"
    user: "${DB_USER:kanban}"
    password: "${DB_PASSWORD}"
```

## Безопасность
- Пароль БД — из переменной окружения `DB_PASSWORD`, не в репозитории
- SSL-соединение — обязательно в production
- Миграции — отдельный пользователь (flyway) или те же credentials
- SQL-инъекции — через параметризованные R2DBC-запросы (`:param`), никакой конкатенации

## Развёртывание
- Docker-образ: `postgres:16-alpine`
- Порт: 5432
- Volume для данных: `pgdata:/var/lib/postgresql/data`
- Healthcheck: `pg_isready -U kanban`
