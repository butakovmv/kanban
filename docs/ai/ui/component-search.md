# Компоненты: Поиск

## SearchResultsPage

**Страницы:** [08-search](08-search.md)  
**Use Case:** [UC-05-03](../usecase/05-filtering-and-search.md#uc-05-03), [UC-05-04](../usecase/05-filtering-and-search.md#uc-05-04)

**Поведение:** Страница результатов поиска. Загружает результаты на основе query-параметров. Отображает список найденных задач с выделением совпадений. Настройки поиска (чекбоксы) можно изменить и перезапустить поиск.

**Состояния:**
- `idle` — поле пустое, нет запроса
- `searching` — выполняется поиск
- `results` — найдены результаты
- `empty` — ничего не найдено
- `error` — ошибка

**Данные:**
- _Route query:_ q, includeBacklog, includeArchive, includeComments
- _User →_: text, toggle checkboxes, resubmit
- _Store → (search):_ search(projectId, params), results[]
- _API:_ GET /api/v1/projects/:id/search?q=&includeBacklog=&includeArchive=&includeComments=
