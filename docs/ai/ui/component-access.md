# Компоненты: Управление доступом

## AccessControlPage

**Страницы:** [11-access-control](11-access-control.md)  
**Use Case:** [UC-08-01](../usecase/08-access-control.md#uc-08-01), [UC-08-02](../usecase/08-access-control.md#uc-08-02), [UC-08-03](../usecase/08-access-control.md#uc-08-03), [UC-08-04](../usecase/08-access-control.md#uc-08-04)

**Поведение:** Страница со списком групп. При выборе группы — отображает участников и PermissionGrid. Управление группами: создание, добавление/удаление участников.

**Состояния:**
- `loading` — загрузка групп
- `loaded` — список отображён
- `empty` — нет групп (EmptyState с action «Создать группу»)
- `error` — ошибка

**Данные:**
- _User →_: createGroup, selectGroup, addMember, removeMember
- _Store → (access):_ fetchGroups(projectId), groups[], selectedGroup, createGroup(name), addMember(groupId, login), removeMember(groupId, userId)
- _API:_ GET /api/v1/projects/:id/groups, POST …/groups, POST …/groups/:id/members, DELETE …/groups/:id/members/:userId

---

## PermissionGrid

**Страницы:** [11-access-control](11-access-control.md)  
**Use Case:** [UC-08-04](../usecase/08-access-control.md#uc-08-04)

**Поведение:** Таблица разрешений для выбранной группы. Строки — объекты (задачи, колонки, документы). Колонки — действия (просмотр, создание, редактирование, удаление). Ячейки — чекбоксы. Изменения применяются сразу.

**Состояния:**
- `loading` — загрузка прав
- `loaded` — таблица отображена
- `saving` — сохранение (applied immediately)
- `error` — ошибка

**Данные:**
- _Props:_ groupId
- _User →_: toggle checkboxes
- _Store → (access):_ permissions[groupId], updatePermission(groupId, objectType, action, value)
- _API:_ PUT /api/v1/projects/:id/groups/:id/permissions { objectType, action, value }
