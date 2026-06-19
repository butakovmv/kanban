# Компоненты: Детальная страница задачи

## TaskDetailPage

**Страницы:** [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-02](../usecase/04-task-operations.md#uc-04-02), [UC-04-04](../usecase/04-task-operations.md#uc-04-04), [UC-04-06](../usecase/04-task-operations.md#uc-04-06), [UC-04-10](../usecase/04-task-operations.md#uc-04-10), [UC-11-01](../usecase/11-bot-assignee.md#uc-11-01)

**Поведение:** Полная информация о задаче. Отображает и позволяет редактировать все поля: название, описание, статус (выпадающий список колонок), приоритет, исполнитель (выпадающий список пользователей + ботов), Use Case, метки. Даты — только чтение. Содержит вложенные компоненты CommentSection, FileList, TaskHistory.

**Состояния:**
- `loading` — загрузка задачи
- `loaded` — отображена
- `saving` — сохранение изменений
- `error` — ошибка загрузки/сохранения
- `not_found` — задача не существует (404)

**Данные:**
- _Store → (task):_ fetchTask(projectId, taskId), task, updateTask(data)
- _API:_ GET /api/v1/projects/:pid/tasks/:tid, PUT /api/v1/projects/:pid/tasks/:tid
- _WebSocket:_ задача обновлена другим пользователем

---

## CommentSection

**Страницы:** [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-07](../usecase/04-task-operations.md#uc-04-07)

**Поведение:** Список комментариев (автор, дата, текст) + поле ввода нового комментария. Автор может редактировать/удалять свой комментарий.

**Состояния:**
- `loading` — загрузка комментариев
- `loaded` — отображены
- `empty` — нет комментариев (EmptyState)
- `submitting` — отправка
- `error` — ошибка

**Данные:**
- _Props:_ taskId
- _User →_: text (новый комментарий), edit/delete
- _Store → (task):_ comments[], addComment(taskId, text), updateComment(id, text), deleteComment(id)
- _API:_ GET /api/v1/projects/:pid/tasks/:tid/comments, POST …/comments, PUT …/comments/:id, DELETE …/comments/:id

---

## FileList

**Страницы:** [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-08](../usecase/04-task-operations.md#uc-04-08)

**Поведение:** Список прикреплённых файлов с возможностью скачать или удалить. Кнопка «Прикрепить файл» открывает системный файловый диалог.

**Состояния:**
- `loading` — загрузка списка
- `loaded` — отображены файлы
- `empty` — нет файлов (EmptyState)
- `uploading` — файл загружается (прогресс-бар)
- `error` — ошибка

**Данные:**
- _Props:_ taskId
- _User →_: file select / drop, click download, click delete
- _Store → (task):_ files[], uploadFile(taskId, file), deleteFile(fileId)
- _API:_ GET /api/v1/projects/:pid/tasks/:tid/files, POST …/files (multipart), DELETE …/files/:id

---

## TaskHistory

**Страницы:** [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-06](../usecase/04-task-operations.md#uc-04-06)

**Поведение:** Лента изменений задачи: кто, когда, что изменил. Автоматически заполняется системой. Только чтение.

**Состояния:**
- `loading` — загрузка истории
- `loaded` — отображена
- `empty` — нет изменений

**Данные:**
- _Props:_ taskId
- _User →_: none
- _Store:_ нет (может быть частью task данных)
- _API:_ GET /api/v1/projects/:pid/tasks/:tid/history
