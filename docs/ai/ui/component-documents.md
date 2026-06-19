# Компоненты: Документы

## DocumentPage

**Страницы:** [09-documents](09-documents.md)  
**Use Case:** [UC-06-01](../usecase/06-documents.md#uc-06-01), [UC-06-02](../usecase/06-documents.md#uc-06-02)

**Поведение:** Страница со списком типов документов проекта. Категории отображаются в виде вкладок или аккордеона. Каждый тип — своя секция с кнопкой загрузки, существующими файлами (preview / ссылка на скачивание), кнопкой удаления.

**Состояния:**
- `loading` — загрузка списка документов
- `loaded` — отображены категории
- `empty` — нет документов (в категории или в целом)
- `uploading` — загрузка документа
- `error` — ошибка

**Данные:**
- _User →_: выбор категории, upload, download, delete
- _Store → (documents):_ fetchDocuments(projectId), documentsByType[], uploadDocument(projectId, type, file), deleteDocument(id)
- _API:_ GET /api/v1/projects/:id/documents, POST /api/v1/projects/:id/documents { type, file }, DELETE /api/v1/projects/:id/documents/:id
