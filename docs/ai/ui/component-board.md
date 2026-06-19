# Компоненты: Канбан-доска (основная)

## BoardPage

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-03-01](../usecase/03-board-columns-and-swimlanes.md#uc-03-01), [UC-03-02](../usecase/03-board-columns-and-swimlanes.md#uc-03-02), [UC-03-03](../usecase/03-board-columns-and-swimlanes.md#uc-03-03), [UC-03-04](../usecase/03-board-columns-and-swimlanes.md#uc-03-04), [UC-03-05](../usecase/03-board-columns-and-swimlanes.md#uc-03-05), [UC-03-06](../usecase/03-board-columns-and-swimlanes.md#uc-03-06), [UC-03-07](../usecase/03-board-columns-and-swimlanes.md#uc-03-07), [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-04-03](../usecase/04-task-operations.md#uc-04-03), [UC-04-05](../usecase/04-task-operations.md#uc-04-05), [UC-04-09](../usecase/04-task-operations.md#uc-04-09), [UC-04-10](../usecase/04-task-operations.md#uc-04-10), [UC-05-01](../usecase/05-filtering-and-search.md#uc-05-01), [UC-05-02](../usecase/05-filtering-and-search.md#uc-05-02), [UC-09-01](../usecase/09-realtime.md#uc-09-01), [UC-09-02](../usecase/09-realtime.md#uc-09-02)

**Поведение:** Главный контейнер доски. Управляет состоянием доски: колонки, задачи, фильтры, бэклог, архив, исполнители. Подключается к WebSocket для real-time синхронизации. Координирует дочерние компоненты.

**Состояния:**
- `loading` — загрузка доски
- `loaded` — доска отображена
- `error` — ошибка загрузки
- `reconnecting` — потеря WebSocket, попытка переподключения

**Данные:**
- _Store → (board):_ fetchBoard(projectId), boardData, columns[], tasks[], backlog[], archive[], assignees[], filters, realtime
- _WebSocket:_ connect / disconnect, onMessage (create/update/delete column/task)
- _API:_ GET /api/v1/projects/:id/board, WebSocket /ws/projects/:id
- _Restore:_ none (всегда загружает с сервера, кеш в Pinia)

---

## BoardColumn

**Страницы:** [06-board](06-board.md)  
**Use Case:** [UC-03-01](../usecase/03-board-columns-and-swimlanes.md#uc-03-01), [UC-03-02](../usecase/03-board-columns-and-swimlanes.md#uc-03-02), [UC-03-03](../usecase/03-board-columns-and-swimlanes.md#uc-03-03), [UC-04-03](../usecase/04-task-operations.md#uc-04-03), [UC-04-05](../usecase/04-task-operations.md#uc-04-05)

**Поведение:** Колонка доски. Отображает заголовок, счётчик задач, список свимлайнов с TaskCard. Drag-n-drop зона (принимает задачи из других колонок и бэклога). Меню: редактировать, удалить.

**Состояния:**
- `default` — отображена
- `dragOver` — задача наведена для дропа
- `editing` — редактирование названия (inline)
- `deleting` — подтверждение удаления (ConfirmDialog)

**Данные:**
- _Props:_ column (id, name, order, tasks)
- _User →_: drag-n-drop (native drag events), click menu
- _Store → (board):_ updateColumn(id, name), deleteColumn(id), moveTask(taskId, toColumnId, position)
- _API:_ POST /api/v1/projects/:id/columns, PUT /api/v1/projects/:id/columns/:id, DELETE /api/v1/projects/:id/columns/:id
- _WebSocket:_ колонка создана/удалена/переименована другим пользователем
