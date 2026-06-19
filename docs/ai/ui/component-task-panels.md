# Компоненты: Карточка задачи, свимлайн, панели

## TaskCard

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-04-03](../usecase/04-task-operations.md#uc-04-03), [UC-04-05](../usecase/04-task-operations.md#uc-04-05), [UC-04-10](../usecase/04-task-operations.md#uc-04-10), [UC-05-01](../usecase/05-filtering-and-search.md#uc-05-01), [UC-05-02](../usecase/05-filtering-and-search.md#uc-05-02)

**Поведение:** Минималистичная карточка задачи на доске. Отображает: название, метки (LabelChip), приоритет (PriorityBadge), исполнитель (UserAvatar). Поддерживает drag-n-drop для перемещения между колонками и сортировки внутри колонки. Клик открывает новую вкладку с TaskDetailPage.

**Состояния:**
- `default` — отображена
- `dragging` — перетаскивается (полупрозрачная копия)
- `dragOver` — над карточкой зависла другая (для сортировки)
- `filtered_out` — скрыта фильтром (или серая)

**Данные:**
- _Props:_ task (id, title, priority, labels, assignee, useCase, status, columnId)
- _User →_: drag-start / drag-end / click
- _Store → (board):_ moveTask(taskId, fromColumn, toColumn, position), updateTaskOrder(taskId, newPosition)
- _API:_ PUT /api/v1/projects/:id/tasks/:id/position { columnId, position }
- _WebSocket:_ задача перемещена другим пользователем

---

## Swimlane

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-03-06](../usecase/03-board-columns-and-swimlanes.md#uc-03-06), [UC-03-07](../usecase/03-board-columns-and-swimlanes.md#uc-03-07)

**Поведение:** Группировка задач по Use Case внутри колонки. Заголовок — название Use Case. Дефолтный свимлайн «Bug Fix» для задач без Use Case. Автоматически появляется/исчезает.

**Состояния:**
- `default` — содержит задачи
- `empty` — скрывается (когда последняя задача покинула колонку)
- `default_bugfix` — дефолтный свимлайн

**Данные:**
- _Props:_ name, tasks[], isDefault
- _Store:_ нет (вычисляется из useCase задач в BoardColumn)
- _API:_ нет (frontend-only, группировка по useCase)

---

## BacklogPanel

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-03-04](../usecase/03-board-columns-and-swimlanes.md#uc-03-04), [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-04-05](../usecase/04-task-operations.md#uc-04-05)

**Поведение:** Выдвижная панель слева. Список задач без статуса, отсортированных по приоритету. Drag-n-drop из панели в колонку (создаёт задачу на доске и меняет статус). Поддерживает фильтр, если активен.

**Состояния:**
- `hidden` — скрыта
- `visible` — отображена
- `loading` — загрузка бэклога
- `empty` — нет задач

**Данные:**
- _Props:_ visible, filter
- _User →_: drag-n-drop в колонку
- _Store → (board):_ backlog[], moveFromBacklog(taskId, columnId, position)
- _API:_ GET /api/v1/projects/:id/backlog, PUT /api/v1/projects/:id/tasks/:id/move { columnId, position }

---

## ArchivePanel

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-03-05](../usecase/03-board-columns-and-swimlanes.md#uc-03-05), [UC-04-09](../usecase/04-task-operations.md#uc-04-09)

**Поведение:** Выдвижная панель справа. Список архивных задач, отсортированных по дате архивирования (более поздние выше). Принимает задачи через drag-n-drop из колонок. Поддерживает фильтр, если активен.

**Состояния:**
- `hidden` — скрыта
- `visible` — отображена
- `loading` — загрузка архива
- `empty` — нет задач

**Данные:**
- _Props:_ visible, filter
- _User →_: drag-n-drop из колонки
- _Store → (board):_ archive[], moveToArchive(taskId)
- _API:_ PUT /api/v1/projects/:id/tasks/:id/archive

---

## AssigneePanel

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-05-02](../usecase/05-filtering-and-search.md#uc-05-02), [UC-04-10](../usecase/04-task-operations.md#uc-04-10)

**Поведение:** Выдвижная панель снизу. Список исполнителей с аватаром (UserAvatar), именем и счётчиком задач. Клик по исполнителю — фильтр. Под каждым — список его задач (клик → TaskDetailPage).

**Состояния:**
- `hidden` — скрыта
- `visible` — отображена
- `loading` — загрузка
- `empty` — нет исполнителей

**Данные:**
- _Props:_ visible
- _User →_: click on assignee → filter, click on task → navigate
- _Store → (board):_ assignees[], setFilter({ assignee })
- _API:_ GET /api/v1/projects/:id/assignees

---

## FilterPanel

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-05-01](../usecase/05-filtering-and-search.md#uc-05-01)

**Поведение:** Панель фильтрации. Позволяет выбрать значения по признакам: приоритет, исполнитель, метка, Use Case. Фильтр применяется к колонкам, к бэклогу и архиву — только если открыты. Кнопка сброса.

**Состояния:**
- `collapsed` — свёрнут (только иконка фильтра)
- `expanded` — развёрнут
- `active` — хотя бы один фильтр применён (визуальный индикатор)
- `resetting` — сброс фильтров

**Данные:**
- _User →_: выбор значений в каждом дропдауне/чипсе
- _Store → (board):_ setFilters(filters), resetFilters(), activeFilters
- _API:_ нет (фильтрация на клиенте, данные уже загружены)

---

## SearchBar

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-05-03](../usecase/05-filtering-and-search.md#uc-05-03), [UC-05-04](../usecase/05-filtering-and-search.md#uc-05-04)

**Поведение:** Поле поиска с кнопкой. При вводе и нажатии Enter — открывает `/projects/:id/search?q=...` в новой вкладке. Может показывать дополнительные настройки перед поиском.

**Состояния:**
- `idle` — пустое поле
- `focused` — поле в фокусе
- `hasText` — введён текст

**Данные:**
- _User →_: text input, submit
- _Props:_ projectId
- _Store:_ нет
- _API:_ нет (редирект на страницу поиска)
