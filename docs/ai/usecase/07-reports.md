# Сценарии использования: Отчёты

---

<a id="uc-07-01"></a>
## UC-07-01: Просмотр Cumulative Flow Diagram
**Актор:** Участник проекта  
**Цель:** Проанализировать динамику накопления задач по колонкам  
**Предусловия:** На доске есть достаточно данных для построения графика  
**Постусловия:** Отображён график CFD  

```mermaid
sequenceDiagram
    actor Member as Участник
    participant System as Система

    Member->>System: Открытие раздела «Отчёты»
    Member->>System: Выбор «Cumulative Flow Diagram»
    System->>System: Сбор данных по задачам и колонкам
    System-->>Member: График CFD
```

**Связанный сценарий:** [US-07-01](../userstory/07-reports.md#us-07-01)

---

<a id="uc-07-02"></a>
## UC-07-02: Просмотр Lead Time Distribution Chart
**Актор:** Участник проекта  
**Цель:** Проанализировать распределение времени выполнения задач  
**Предусловия:** Есть завершённые задачи  
**Постусловия:** Отображена гистограмма  

```mermaid
sequenceDiagram
    actor Member as Участник
    participant System as Система

    Member->>System: Открытие раздела «Отчёты»
    Member->>System: Выбор «Lead Time Distribution Chart»
    System->>System: Расчёт времени выполнения задач
    System-->>Member: Гистограмма распределения
```

**Связанный сценарий:** [US-07-02](../userstory/07-reports.md#us-07-02)

---

<a id="uc-07-03"></a>
## UC-07-03: Просмотр диаграммы Гантта по Use Case
**Актор:** Участник проекта  
**Цель:** Увидеть временную шкалу задач, сгруппированных по Use Case  
**Предусловия:** Есть задачи с назначенным Use Case  
**Постусловия:** Отображена диаграмма Гантта  

```mermaid
sequenceDiagram
    actor Member as Участник
    participant System as Система

    Member->>System: Открытие раздела «Отчёты»
    Member->>System: Выбор «Диаграмма Гантта по Use Case»
    System->>System: Группировка задач по Use Case, построение шкалы
    System-->>Member: Диаграмма Гантта
```

**Связанный сценарий:** [US-07-03](../userstory/07-reports.md#us-07-03)

---

<a id="uc-07-04"></a>
## UC-07-04: Просмотр лога событий
**Актор:** Участник проекта  
**Цель:** Просмотреть историю изменений на доске  
**Предусловия:** Есть залогированные события  
**Постусловия:** Отображена страница лога  

```mermaid
sequenceDiagram
    actor Member as Участник
    participant System as Система

    Member->>System: Открытие раздела «Отчёты»
    Member->>System: Выбор «Лог событий»
    System->>System: Загрузка последних 100 событий
    System-->>Member: Список событий
    Member->>System: Нажатие «Загрузить ещё»
    System->>System: Загрузка следующих 100 событий
    System-->>Member: Дополненный список
```

**Связанный сценарий:** [US-07-04](../userstory/07-reports.md#us-07-04)
