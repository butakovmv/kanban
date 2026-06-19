# Компоненты: Аутентификация

## LoginForm

**Страницы:** [01-login](01-login.md)  
**Use Case:** [UC-01-02](../usecase/01-auth-and-profile.md#uc-01-02), [UC-01-03](../usecase/01-auth-and-profile.md#uc-01-03)

**Поведение:** Форма входа с переключателем способа (пароль / TOTP). При отправке валидирует поля и вызывает API. При успехе — перенаправляет на `/projects`. При ошибке — подсвечивает поля и показывает сообщение.

**Состояния:**
- `idle` — форма пуста, ожидает ввод
- `validating` — валидация полей
- `submitting` — отправка запроса на бэкенд (кнопка заблокирована)
- `error` — ошибка аутентификации
- `success` — успешный вход (редирект)

**Данные:**
- _User →_: login, password или totpCode, mode ('password' | 'totp')
- _Component →:_ хранит локально login, password/totpCode, mode
- _Store → (auth):_ login(credentials) → API POST /auth/login
- _API ← Store:_ { token, user }
- _API:_ POST /api/v1/auth/login { login, password? | totpCode? }

---

## RegisterForm

**Страницы:** [02-register](02-register.md)  
**Use Case:** [UC-01-01](../usecase/01-auth-and-profile.md#uc-01-01)

**Поведение:** Форма регистрации. Валидирует логин (уникальность) и пароль (минимальная длина). При успехе — перенаправляет на `/login`.

**Состояния:**
- `idle` — форма пуста
- `validating` — проверка полей
- `submitting` — отправка
- `error` — логин занят / ошибка валидации
- `success` — редирект на `/login`

**Данные:**
- _User →_: login, password
- _Store → (auth):_ register(credentials) → API POST /auth/register
- _API:_ POST /api/v1/auth/register { login, password }

---

## RecoverForm

**Страницы:** [03-recover](03-recover.md)  
**Use Case:** [UC-01-05](../usecase/01-auth-and-profile.md#uc-01-05)

**Поведение:** Два шага: (1) ввод email → отправка ссылки, (2) ввод нового пароля (после перехода по ссылке). На первом шаге проверяет формат email.

**Состояния:**
- `step1_idle` — запрос email
- `step1_submitting` — отправка email
- `step1_success` — ссылка отправлена
- `step1_error` — email не найден
- `step2_idle` — форма нового пароля
- `step2_submitting` — отправка нового пароля
- `step2_success` — пароль изменён

**Данные:**
- _User →_: email, newPassword, token (из URL)
- _Store → (auth):_ requestReset(email), confirmReset(token, newPassword)
- _API:_ POST /api/v1/auth/recover-request { email }, POST /api/v1/auth/recover-confirm { token, newPassword }

---

## TotpSetup

**Страницы:** [04-profile](04-profile.md)  
**Use Case:** [UC-01-04](../usecase/01-auth-and-profile.md#uc-01-04)

**Поведение:** Мастер привязки TOTP. Шаги: (1) подтверждение текущим паролем → (2) отображение QR-кода и секрета → (3) ввод первого кода для подтверждения. После успеха — TOTP активирован, кнопка входа заменяет пароль.

**Состояния:**
- `idle` — отображает статус (привязан/не привязан)
- `confirming_password` — запрос текущего пароля
- `showing_qr` — отображение QR + поле для первого кода
- `verifying` — проверка кода
- `active` — TOTP привязан
- `error` — ошибка на любом шаге

**Данные:**
- _User →_: currentPassword, firstTotpCode
- _Store → (auth):_ setupTotp(password) → API, verifyTotp(code) → API
- _API:_ POST /api/v1/auth/totp/setup { password }, POST /api/v1/auth/totp/verify { code }, DELETE /api/v1/auth/totp
