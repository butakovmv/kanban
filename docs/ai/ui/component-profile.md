# Компоненты: Профиль и тариф

## ProfilePage

**Страницы:** [04-profile](04-profile.md)  
**Use Case:** [UC-01-04](../usecase/01-auth-and-profile.md#uc-01-04), [UC-01-06](../usecase/01-auth-and-profile.md#uc-01-06), [UC-01-07](../usecase/01-auth-and-profile.md#uc-01-07)

**Поведение:** Страница профиля. Содержит секции: безопасность (TOTP), тариф, управление сессией. Выступает контейнером для TotpSetup и TariffInfo.

**Состояния:**
- `loading` — загрузка данных профиля
- `loaded` — отображён

**Данные:**
- _Store → (auth):_ fetchProfile(), user
- _API:_ GET /api/v1/auth/profile

---

## TariffInfo

**Страницы:** [04-profile](04-profile.md)  
**Use Case:** [UC-01-07](../usecase/01-auth-and-profile.md#uc-01-07), [UC-10-02](../usecase/10-admin.md#uc-10-02)

**Поведение:** Отображает название текущего тарифного плана и лимиты с индикаторами использования (прогресс-бары: проекты X/Y, колонки X/Y, исполнители X/Y). Сообщает о превышении лимита, если действие заблокировано.

**Состояния:**
- `loaded` — информация отображена
- `limit_reached` — один из лимитов исчерпан (визуальное предупреждение)

**Данные:**
- _Props:_ tariff (name, limits, usage)
- _User →_: none (только чтение)
- _Store → (auth):_ tariff
- _API:_ часть GET /api/v1/auth/profile
