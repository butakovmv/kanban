# Компоненты: Проекты (список и настройки)

## ProjectList

**Страницы:** [05-projects](05-projects.md)  
**Use Case:** [UC-02-01](../usecase/02-project-management.md#uc-02-01)

**Поведение:** Сетка/список проектов пользователя. Загружает список при монтировании. Каждый элемент — ProjectCard. Кнопка «Новый проект» открывает ProjectCreateForm.

**Состояния:**
- `loading` — загрузка списка
- `loaded` — отображены проекты
- `empty` — нет проектов (EmptyState с action «Создать проект»)
- `error` — ошибка загрузки

**Данные:**
- _Store → (projects):_ fetchList(), projects[]
- _API:_ GET /api/v1/projects

---

## ProjectCard

**Страницы:** [05-projects](05-projects.md)  
**Use Case:** [UC-02-01](../usecase/02-project-management.md#uc-02-01), [UC-02-02](../usecase/02-project-management.md#uc-02-02), [UC-02-03](../usecase/02-project-management.md#uc-02-03)

**Поведение:** Карточка проекта. Отображает название, дату создания, количество задач. Клик → `/projects/:id/board`. Меню: настройки, удалить.

**Состояния:**
- `default` — отображён
- `deleting` — удаление (подтверждение через ConfirmDialog)

**Данные:**
- _Props:_ project (id, name, createdAt, taskCount)
- _Emits:_ delete, edit
- _Store:_ нет (данные из родителя)
- _API:_ DELETE /api/v1/projects/:id

---

## ProjectCreateForm

**Страницы:** [05-projects](05-projects.md)  
**Use Case:** [UC-02-01](../usecase/02-project-management.md#uc-02-01)

**Поведение:** Модалка создания проекта. Поле «Название». Валидация на пустоту. При успехе — проект добавляется в список, редирект на его доску.

**Состояния:**
- `hidden` — скрыта
- `shown` — открыта
- `submitting` — отправка
- `error` — ошибка / лимит исчерпан

**Данные:**
- _User →_: name
- _Store → (projects):_ create(name) → API
- _API:_ POST /api/v1/projects { name }

---

## ProjectSettings

**Страницы:** [13-project-settings](13-project-settings.md)  
**Use Case:** [UC-02-02](../usecase/02-project-management.md#uc-02-02), [UC-02-03](../usecase/02-project-management.md#uc-02-03)

**Поведение:** Форма редактирования названия проекта + зона удаления. Удаление требует двойного подтверждения.

**Состояния:**
- `loading` — загрузка данных проекта
- `loaded` — форма отображена
- `saving` — сохранение изменений
- `deleting` — удаление (ConfirmDialog)
- `error` — ошибка

**Данные:**
- _User →_: name (edit), confirmDelete (delete)
- _Store → (projects):_ update(id, data), delete(id)
- _API:_ PUT /api/v1/projects/:id { name }, DELETE /api/v1/projects/:id
