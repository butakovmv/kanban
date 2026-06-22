.PHONY: update-back update-front dev
.PHONY: build-front build-front-quick watch-front dev-front

# === Бэкенд ===

# Пересобрать изменённые файлы в *.jar артефакт
update-back:
	./gradlew :spring:bootJar --no-daemon

# === Фронтенд ===

# Быстрая сборка фронта без typecheck (только vite build)
update-front:
	cd vue && npx vite build

# Запустить Docker Compose в режиме слежения
dev:
	docker compose up --watch

# Запустить Docker Compose в режиме слежения
e2e:
	cd ./e2etest && npm run test
