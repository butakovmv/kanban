.PHONY: update-back update-front dev
.PHONY: build-front build-front-quick watch-front dev-front

# === Бэкенд ===

# Пересобрать изменённые файлы в *.jar артефакт
update-back:
	./gradlew :spring:bootJar --no-daemon

# Следить за файлами бэка и пересобирать при изменениях
watch-back:
	./gradlew assemble --no-daemon -t

# === Фронтенд ===

# Быстрая сборка фронта без typecheck (только vite build)
update-front:
	cd vue && npx vite build

# Следить за файлами фронта и пересобирать при изменениях
watch-front:
	cd vue && npx vite build --watch

# Запустить Docker Compose в режиме слежения
dev:
	docker compose up --watch

# Запустить Docker Compose в режиме слежения
e2e:
	cd ./e2etest && npm run test
