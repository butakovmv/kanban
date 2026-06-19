# Домен: Управление доступом (Access Control)

## Обзор
Разграничение прав доступа к проектам через группы пользователей. Владелец проекта создаёт группы, назначает права (task:read, task:create, column:create, document:read и т.д.) и добавляет/удаляет участников.

**Границы:**
- Не включает аутентификацию (см. `identity-and-access`)
- Не включает глобальные права (администратор — также часть `identity-and-access`)
- Права проверяются на уровне сервиса при каждой операции

## Агрегаты

### `Group` (корень агрегата)
Группа пользователей внутри проекта с набором прав.

- **id:** `GroupId` (value object)
- **projectId:** `ProjectId` (value object)
- **name:** String (1-100)
- **permissions:** `Permissions` (value object)
- **memberIds:** `List<UserId>` (список участников)

**Правила:**
- Владелец проекта всегда имеет полный доступ (проверка прав не применяется к владельцу)
- Группа не может быть пустой, если она является единственной группой проекта (всегда должен быть хотя бы один участник с правами)
- При удалении группы участники теряют доступ к проекту (если не состоят в других группах)

## Объекты-значения

| Объект | Поля | Инварианты |
|---|---|---|
| `GroupId` | `value: UUID` | Не null |
| `Permissions` | `taskRead, taskCreate, taskUpdate, taskDelete: Boolean, columnCreate, columnUpdate, columnDelete: Boolean, documentRead, documentCreate, documentUpdate, documentDelete: Boolean` | — |

## Операции

| Операция | Команда | Агрегат | Событие |
|---|---|---|---|
| Список групп | `ListGroups(projectId)` | Group | — (query) |
| Создание группы | `CreateGroup(projectId, name)` | Group | `GroupCreated` |
| Удаление группы | `DeleteGroup(groupId)` | Group | `GroupDeleted` |
| Добавление участника | `AddMember(groupId, userId)` | Group | `MemberAdded` |
| Удаление участника | `RemoveMember(groupId, userId)` | Group | `MemberRemoved` |
| Настройка прав | `SetPermissions(groupId, permissions)` | Group | `PermissionsUpdated` |
| Проверка права | `CheckPermission(userId, projectId, permission)` | Group | — (query) |

## Связанные Use Case
- [UC-08-01](../usecase/08-access-control.md#uc-08-01) … [UC-08-04](../usecase/08-access-control.md#uc-08-04)

## Связанные API
- `docs/ai/api/access.md` — 6 эндпоинтов
