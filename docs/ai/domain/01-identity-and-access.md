# Домен: Идентификация и доступ (Identity & Access)

## Обзор
Управление учётными записями пользователей, аутентификация (пароль, TOTP), восстановление доступа, сессии, тарифные планы. Административные операции (поиск пользователей, смена тарифа) также входят в этот контекст.

**Границы:**
- Не включает управление доступом к проектам (группы и права — отдельный домен `access-control`)
- Не включает профиль пользователя (редактирование данных) — отложено

## Агрегаты

### `User` (корень агрегата)
Учётная запись пользователя.

- **id:** `UserId` (value object)
- **login:** `Login` (value object, 3-50 символов, латиница/цифры/`_`)
- **passwordHash:** `PasswordHash` (value object)
- **email:** `Email` (value object, макс. 254 символа)
- **totpSecret:** `TotpSecret?` (value object, base32)
- **tariffId:** `TariffId` (value object)
- **createdAt:** Instant

**Правила:**
- Логин уникален в системе
- Password хранится в виде bcrypt-хеша
- TOTP может быть не привязан; при привязке проходит двухэтапное подтверждение (пароль + первый код)
- При смене пароля (через recover) TOTP сбрасывается

### `Session` (entity)
Сессия пользователя (JWT-токен).

- **token:** JWT (value object)
- **userId:** UserId
- **issuedAt:** Instant
- **expiresAt:** Instant

**Правила:**
- Токен stateless — сессия не хранится в БД
- refresh token не предусмотрен; короткое время жизни access token

### `Tariff` (entity)
Тарифный план.

- **id:** `TariffId` (value object)
- **name:** String (unique)
- **limits:** `TariffLimits` (value object)
  - maxProjects: Int (по умолчанию 2)
  - maxColumns: Int (по умолчанию 10)
  - maxAssignees: Int (по умолчанию 5)

## Сущности

(все значимые сущности перечислены выше как агрегаты; отдельные сущности не выделяются)

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `UserId` | `value: UUID` | Не null |
| `Login` | `value: String` | 3-50, латиница/цифры/`_`, not blank |
| `Email` | `value: String` | RFC 5321, макс. 254 |
| `PasswordHash` | `value: String` | bcrypt hash (60 символов) |
| `TotpSecret` | `value: String` | base32, 16-32 символа |
| `JwtToken` | `value: String` | JWT compact serialization |
| `TariffId` | `value: UUID` | Не null |
| `TariffLimits` | `maxProjects`, `maxColumns`, `maxAssignees` | Все >= 1 |

## Операции

| Операция | Команда | Агрегат | Событие |
|---|---|---|---|
| Регистрация | `RegisterUser(login, password, email)` | User | `UserRegistered` |
| Вход по паролю | `LoginWithPassword(login, password)` | Session | `UserLoggedIn` |
| Вход по TOTP | `LoginWithTotp(login, totpCode)` | Session | `UserLoggedIn` |
| Привязка TOTP | `BindTotp(userId, password)` | User | `TotpBound` |
| Подтверждение TOTP | `VerifyTotpBinding(userId, totpCode)` | User | `TotpVerified` |
| Отвязка TOTP | `UnbindTotp(userId, password)` | User | `TotpUnbound` |
| Восстановление доступа | `RequestRecovery(email)` | — | `RecoveryEmailSent` |
| Сброс пароля | `ResetPassword(token, newPassword)` | User | `PasswordReset` |
| Выход | `Logout(token)` | Session | `UserLoggedOut` |
| Создание тарифа | `CreateTariff(name, limits)` | Tariff | `TariffCreated` |
| Смена тарифа | `ChangeUserTariff(userId, tariffName)` | User | `UserTariffChanged` |
| Поиск пользователей | `SearchUsers(query)` | User | — (query) |

## Связанные Use Case
- [UC-01-01](../usecase/01-auth-and-profile.md#uc-01-01) … [UC-01-07](../usecase/01-auth-and-profile.md#uc-01-07)
- [UC-10-01](../usecase/10-admin.md#uc-10-01) … [UC-10-02](../usecase/10-admin.md#uc-10-02)

## Связанные API
- `docs/ai/api/auth.md` — 11 эндпоинтов
- `docs/ai/api/admin.md` — 2 эндпоинта
