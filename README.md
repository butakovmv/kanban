# Kanban Board

Многопользовательская Kanban-доска с реальным временем, управлением задачами, документами и гибкой системой прав доступа.

## Стек

**Бэкенд:** Kotlin 2.0.21, Spring Boot 3.3.5 (WebFlux), R2DBC, Flyway, PostgreSQL 16, MinIO, NATS

**Фронтенд:** Vue 3 (Composition API, `<script setup lang="ts">`), Vite 6, Pinia, Vue Router 4, SCSS

**Тесты:** JUnit 5, WebTestClient, Vitest, Playwright

## Быстрый старт

```bash
# 1. Клонировать репозиторий
git clone <url>
cd kanban

# 2. Скопировать переменные окружения
cp .env.example .env

# 3. Запустить всё в Docker
docker compose up -d

# 4. Открыть в браузере
#    http://localhost:80 — фронтенд
#    http://localhost:8080/actuator/health — API healthcheck
```

После `docker compose up` будут запущены:
- **PostgreSQL 16** — база данных (порт 5432)
- **MinIO** — S3-совместимое хранилище файлов (порт 9000, консоль 9090)
- **Kanban API** — Spring Boot приложение (порт 8080)
- **Kanban Frontend** — Nginx со статикой Vue SPA (порт 80)

## Разработка

### Требования
- Java 21, Gradle 8.14.1 (обёртка)
- Node.js 22.18, npm 10.9
- Docker + docker compose

### Бэкенд

```bash
./gradlew build              # Сборка + линтеры + тесты
./gradlew ktlintMainCheck    # Kotlin style check
./gradlew detekt             # Static analysis
./gradlew :spring:bootRun    # Запуск dev-сервера (с локальной БД)
```

### Фронтенд

```bash
cd vue
npm install
npm run dev                  # Vite dev-сервер на порту 3000 (/api проксируется на 8080)
npm run build                # Production-сборка
npm run lint                 # ESLint
```

### E2E-тесты

```bash
cd e2etest
npm install
npm run install-browsers     # Установка Playwright-браузеров
npm run test                 # Запуск тестов
```

## CI/CD

- `./gradlew build` — основной CI-пайп (компиляция, ktlint, detekt, тесты)
- `e2etest/scripts/ci.sh` — прогон E2E-тестов в CI

## Структура проекта

```
kanban/
├── spring/       # Точка входа (Spring Boot, Security, Actuator)
├── usecase/      # Чистая бизнес-логика (domain, operation, port)
├── webapi/       # HTTP-адаптер (WebFlux, REST-контроллеры)
├── postgres/     # PostgreSQL-адаптер (R2DBC, Flyway-миграции)
├── nats/         # NATS-адаптер (event publishing, заглушка)
├── vue/          # Vue 3 SPA
└── e2etest/      # Playwright E2E-тесты
```
