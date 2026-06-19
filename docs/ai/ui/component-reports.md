# Компоненты: Отчёты

## ReportsPage

**Страницы:** [10-reports](10-reports.md)  
**Use Case:** [UC-07-01](../usecase/07-reports.md#uc-07-01), [UC-07-02](../usecase/07-reports.md#uc-07-02), [UC-07-03](../usecase/07-reports.md#uc-07-03), [UC-07-04](../usecase/07-reports.md#uc-07-04)

**Поведение:** Страница с вкладками для каждого типа отчёта. Управляет переключением между ChartViewer и EventLogTable.

**Состояния:**
- `loading` — загрузка данных
- `loaded` — отображён выбранный отчёт
- `error` — ошибка

**Данные:**
- _User →_: выбор вкладки, изменение периода
- _Store → (reports):_ activeTab, period, data[tab]
- _API:_ GET /api/v1/projects/:id/reports/:type?period=

---

## ChartViewer

**Страницы:** [10-reports](10-reports.md)  
**Use Case:** [UC-07-01](../usecase/07-reports.md#uc-07-01), [UC-07-02](../usecase/07-reports.md#uc-07-02), [UC-07-03](../usecase/07-reports.md#uc-07-03)

**Поведение:** Отображает график (CFD, Lead Time, Гантт). Использует библиотеку диаграмм (Chart.js / D3). Полноэкранный режим. Выбор периода (день/неделя/месяц/всё).

**Состояния:**
- `loading` — данные загружаются
- `loaded` — график отображён
- `empty` — недостаточно данных
- `error` — ошибка загрузки
- `fullscreen` — полноэкранный режим

**Данные:**
- _Props:_ type ('cfd' | 'leadTime' | 'gantt'), period
- _Emits:_ periodChange
- _Store → (reports):_ chartData[type]
- _API:_ GET /api/v1/projects/:id/reports/:type?period=

---

## EventLogTable

**Страницы:** [10-reports](10-reports.md)  
**Use Case:** [UC-07-04](../usecase/07-reports.md#uc-07-04)

**Поведение:** Таблица событий доски. Колонки: время, пользователь, тип события, описание. Пагинация по 100 записей.

**Состояния:**
- `loading` — загрузка
- `loaded` — таблица отображена
- `empty` — нет событий
- `loading_more` — подгрузка следующих 100
- `all_loaded` — все события загружены (кнопка скрыта)
- `error` — ошибка

**Данные:**
- _User →_: нажатие «Загрузить ещё»
- _Store → (reports):_ events[], page, hasMore
- _API:_ GET /api/v1/projects/:id/reports/events?offset=&limit=100
