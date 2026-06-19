# Компоненты: Администрирование

## AdminPage

**Страницы:** [12-admin](12-admin.md)  
**Use Case:** [UC-10-01](../usecase/10-admin.md#uc-10-01)

**Поведение:** Панель администратора. Поиск пользователя по логину или email. Отображение информации о пользователе (текущий тариф, использование лимитов). Выбор нового тарифа и кнопка применения.

**Состояния:**
- `idle` — поле поиска пусто
- `searching` — поиск пользователя
- `found` — пользователь найден, отображена информация
- `not_found` — пользователь не найден
- `changing_tariff` — изменение тарифа сохраняется
- `changed` — тариф успешно изменён
- `error` — ошибка

**Данные:**
- _User →_: search query, select tariff, confirm
- _Store → (admin):_ searchUser(query), user, tariffs[], changeTariff(userId, tariffId)
- _API:_ GET /api/v1/admin/users?q=, GET /api/v1/admin/tariffs, PUT /api/v1/admin/users/:id/tariff { tariffId }
