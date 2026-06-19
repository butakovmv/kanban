# Компоненты: Общие / переиспользуемые

## ConfirmDialog

**Страницы:** все, где есть опасные действия  
**Use Case:** [UC-02-03](../usecase/02-project-management.md#uc-02-03), [UC-03-03](../usecase/03-board-columns-and-swimlanes.md#uc-03-03), [UC-01-04](../usecase/01-auth-and-profile.md#uc-01-04), [UC-08-03](../usecase/08-access-control.md#uc-08-03), [UC-10-01](../usecase/10-admin.md#uc-10-01)

**Поведение:** Модальное окно с заголовком, сообщением, кнопками «Подтвердить» / «Отмена». Блокирует фон до ответа.

**Состояния:**
- `hidden` — скрыт
- `shown` — отображён с сообщением
- `loading` — кнопка подтверждения в состоянии загрузки (ожидание API)
- `error` — ошибка после подтверждения

**Данные:**
- _Props:_ title, message, confirmText, cancelText, variant (danger/warning/info)
- _Emits:_ confirm, cancel
- _Store:_ нет
- _API:_ нет (управляется родителем)

---

## LoadingSpinner

**Страницы:** все  
**Use Case:** все

**Поведение:** Визуальный индикатор загрузки (спиннер / skeleton / прогресс-бар).

**Состояния:**
- `loading` — спиннер крутится
- `hidden` — скрыт

**Данные:**
- _Props:_ size, label (опционально)
- _Store:_ нет
- _API:_ нет

---

## EmptyState

**Страницы:** все со списками  
**Use Case:** [UC-02-01](../usecase/02-project-management.md#uc-02-01), [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-05-03](../usecase/05-filtering-and-search.md#uc-05-03), [UC-06-01](../usecase/06-documents.md#uc-06-01), [UC-07-04](../usecase/07-reports.md#uc-07-04)

**Поведение:** Отображает иконку, заголовок и описание когда список пуст. Может содержать action-кнопку.

**Состояния:**
- `shown` — отображён
- `hidden` — скрыт (есть данные)

**Данные:**
- _Props:_ icon, title, description, actionLabel, actionLink
- _Emits:_ action
- _Store:_ нет
- _API:_ нет

---

## PriorityBadge

**Страницы:** [06-board](06-board.md), [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-04-02](../usecase/04-task-operations.md#uc-04-02)

**Поведение:** Цветной индикатор приоритета задачи. Цвета: low=серый, medium=синий, high=оранжевый, critical=красный.

**Состояния:** всегда видим

**Данные:**
- _Props:_ priority: 'low' | 'medium' | 'high' | 'critical'
- _Store:_ нет
- _API:_ нет

---

## LabelChip

**Страницы:** [06-board](06-board.md), [07-task-detail](07-task-detail.md)  
**Use Case:** [UC-04-01](../usecase/04-task-operations.md#uc-04-01), [UC-04-02](../usecase/04-task-operations.md#uc-04-02)

**Поведение:** Чипс метки задачи. Отображает текст метки. Может быть кликабельным (фильтр).

**Состояния:**
- `default` — просто отображение
- `clickable` — клик применяет фильтр по метке

**Данные:**
- _Props:_ label, color, clickable
- _Emits:_ click
- _Store:_ нет
- _API:_ нет

---

## UserAvatar

**Страницы:** [06-board](06-board.md), [07-task-detail](07-task-detail.md), [05-projects](05-projects.md), [11-access-control](11-access-control.md), [12-admin](12-admin.md)  
**Use Case:** [UC-04-10](../usecase/04-task-operations.md#uc-04-10), [UC-05-02](../usecase/05-filtering-and-search.md#uc-05-02), [UC-08-02](../usecase/08-access-control.md#uc-08-02), [UC-08-03](../usecase/08-access-control.md#uc-08-03)

**Поведение:** Аватар пользователя. Отображает инициалы на цветном фоне если нет фото, или миниатюру фото.

**Состояния:**
- `default` — с аватаром
- `initials` — без фото, показаны инициалы
- `bot` — специальная иконка для бота

**Данные:**
- _Props:_ user (id, name, avatarUrl, isBot), size
- _Store:_ нет
- _API:_ нет
