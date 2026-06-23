# Компоненты: Документы

## DocumentPage

**Страницы:** [09-documents](09-documents.md)  
**Use Case:** [UC-06-01](../usecase/06-documents.md#uc-06-01), [UC-06-02](../usecase/06-documents.md#uc-06-02)

**Поведение:** Страница со списком документов проекта в виде дерева (иерархические пути). Каждый сегмент пути — раскрываемая папка. Документы отображаются как листья дерева. Кнопка «Создать» открывает форму с полями: путь (например `docs/requirements/`), заголовок, содержимое (markdown). Клик по документу открывает редактор markdown.

**Состояния:**
- `loading` — загрузка списка документов
- `loaded` — отображено дерево документов
- `empty` — нет документов
- `creating` — создание документа
- `editing` — редактирование документа
- `error` — ошибка

**Данные:**
- _User →_: create, read, update, delete document
- _Store → (documents):_ fetchDocuments(projectId), documents[], createDocument(data), updateDocument(id, data), deleteDocument(id)
- _API:_ GET /api/v1/projects/:id/documents, POST /api/v1/documents, PUT /api/v1/documents/:id, DELETE /api/v1/documents/:id
