import { get, post, put, del } from '../../fetch'

export interface Group {
  id: string
  name: string
  description: string | null
  createdAt: string
}

export interface GroupMember {
  groupId: string
  userId: string
  addedAt: string
}

export interface Permission {
  id: string
  resource: string
  action: string
  targetId: string | null
  createdAt: string
}

export interface GroupPermission {
  groupId: string
  permissionId: string
  grantedAt: string
}

export interface PermissionCheck {
  allowed: boolean
  reason: string | null
}

export interface CreateGroupRequest {
  name: string
  description?: string | null
}

export interface UpdateGroupRequest {
  name?: string
  description?: string | null
}

export interface CreatePermissionRequest {
  resource: string
  action: string
  targetId?: string | null
}

export interface AddMemberRequest {
  userId: string
}

function toGroup(raw: Group): Group {
  return {
    id: raw.id,
    name: raw.name,
    description: raw.description,
    createdAt: raw.createdAt,
  }
}

function toMember(raw: GroupMember): GroupMember {
  return {
    groupId: raw.groupId,
    userId: raw.userId,
    addedAt: raw.addedAt,
  }
}

function toPermission(raw: Permission): Permission {
  return {
    id: raw.id,
    resource: raw.resource,
    action: raw.action,
    targetId: raw.targetId,
    createdAt: raw.createdAt,
  }
}

export function listGroups(): Promise<Group[]> {
  return get<{ groups: Group[] }>('/groups').then((r) => r.groups.map(toGroup))
}

export function getGroup(id: string): Promise<Group> {
  return get<Group>(`/groups/${encodeURIComponent(id)}`).then(toGroup)
}

export function createGroup(req: CreateGroupRequest): Promise<Group> {
  const body: Record<string, unknown> = { name: req.name }
  if (req.description !== undefined) {
    body['description'] = req.description
  }
  return post<Group>('/groups', body).then(toGroup)
}

export function updateGroup(id: string, req: UpdateGroupRequest): Promise<Group> {
  const body: Record<string, unknown> = {}
  if (req.name !== undefined) body['name'] = req.name
  if (req.description !== undefined) body['description'] = req.description
  return put<Group>(`/groups/${encodeURIComponent(id)}`, body).then(toGroup)
}

export function deleteGroup(id: string): Promise<void> {
  return del<void>(`/groups/${encodeURIComponent(id)}`)
}

export function listMembers(groupId: string): Promise<GroupMember[]> {
  return get<{ members: GroupMember[] }>(`/groups/${encodeURIComponent(groupId)}/members`).then(
    (r) => r.members.map(toMember),
  )
}

export function addMember(groupId: string, userId: string): Promise<void> {
  return post<void>(`/groups/${encodeURIComponent(groupId)}/members`, {
    user_id: userId,
  })
}

export function removeMember(groupId: string, userId: string): Promise<void> {
  return del<void>(
    `/groups/${encodeURIComponent(groupId)}/members/${encodeURIComponent(userId)}`,
  )
}

export function listUserGroups(userId: string): Promise<Group[]> {
  return get<{ groups: Group[] }>(`/users/${encodeURIComponent(userId)}/groups`).then(
    (r) => r.groups.map(toGroup),
  )
}

export function createPermission(req: CreatePermissionRequest): Promise<Permission> {
  const body: Record<string, unknown> = {
    resource: req.resource,
    action: req.action,
  }
  if (req.targetId !== undefined) {
    body['target_id'] = req.targetId
  }
  return post<Permission>('/permissions', body).then(toPermission)
}

export function deletePermission(id: string): Promise<void> {
  return del<void>(`/permissions/${encodeURIComponent(id)}`)
}

export function findPermissions(
  resource: string,
  targetId?: string | null,
): Promise<Permission[]> {
  let url = `/permissions?resource=${encodeURIComponent(resource)}`
  if (targetId !== undefined && targetId !== null) {
    url += `&target_id=${encodeURIComponent(targetId)}`
  }
  return get<{ permissions: Permission[] }>(url).then((r) => r.permissions.map(toPermission))
}

export function grantPermission(groupId: string, permissionId: string): Promise<void> {
  return post<void>(`/groups/${encodeURIComponent(groupId)}/permissions`, {
    permission_id: permissionId,
  })
}

export function revokePermission(groupId: string, permissionId: string): Promise<void> {
  return del<void>(
    `/groups/${encodeURIComponent(groupId)}/permissions/${encodeURIComponent(permissionId)}`,
  )
}

export function listGroupPermissions(groupId: string): Promise<Permission[]> {
  return get<{ permissions: Permission[] }>(
    `/groups/${encodeURIComponent(groupId)}/permissions`,
  ).then((r) => r.permissions.map(toPermission))
}

export function checkPermission(
  userId: string,
  resource: string,
  action: string,
  targetId?: string | null,
): Promise<PermissionCheck> {
  const params = new URLSearchParams()
  params.set('user_id', userId)
  params.set('resource', resource)
  params.set('action', action)
  if (targetId !== undefined && targetId !== null) {
    params.set('target_id', targetId)
  }
  return get<PermissionCheck>(`/permissions/check?${params.toString()}`)
}
