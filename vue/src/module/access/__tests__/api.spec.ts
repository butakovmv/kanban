import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'
import { accessGenerator } from './accessGenerator'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    del: vi.fn(),
  }
})

describe('access api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('listGroups', () => {
    it('sends GET to /groups and returns items', async () => {
      const items = accessGenerator.groups(3)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.listGroups()

      expect(fetchModule.get).toHaveBeenCalledWith('/groups')
      expect(result).toEqual(items)
      expect(result).toHaveLength(3)
    })
  })

  describe('getGroup', () => {
    it('sends GET to /groups/{id} and returns the group', async () => {
      const group = accessGenerator.group({ id: 'g-1' })
      vi.mocked(fetchModule.get).mockResolvedValue(group)

      const result = await api.getGroup('g-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/groups/g-1')
      expect(result).toEqual(group)
    })
  })

  describe('createGroup', () => {
    it('sends POST to /groups with snake_case body and returns created group', async () => {
      const req = accessGenerator.createGroupRequest({ name: 'New Group', description: 'Desc' })
      const created = accessGenerator.group({ name: 'New Group', description: 'Desc' })
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createGroup(req)

      expect(fetchModule.post).toHaveBeenCalledWith('/groups', {
        name: 'New Group',
        description: 'Desc',
      })
      expect(result).toEqual(created)
    })

    it('omits description when not provided', async () => {
      const req = accessGenerator.createGroupRequest({ description: undefined })
      const created = accessGenerator.group()
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      await api.createGroup(req)

      const body = vi.mocked(fetchModule.post).mock.calls[0]![1] as Record<string, unknown>
      expect(body).not.toHaveProperty('description')
      expect(body).toEqual({ name: req.name })
    })
  })

  describe('updateGroup', () => {
    it('sends PUT to /groups/{id} with provided fields', async () => {
      const updated = accessGenerator.group({ name: 'Renamed' })
      vi.mocked(fetchModule.put).mockResolvedValue(updated)

      const result = await api.updateGroup('g-1', { name: 'Renamed' })

      expect(fetchModule.put).toHaveBeenCalledWith('/groups/g-1', { name: 'Renamed' })
      expect(result).toEqual(updated)
    })
  })

  describe('deleteGroup', () => {
    it('sends DELETE to /groups/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deleteGroup('g-99')

      expect(fetchModule.del).toHaveBeenCalledWith('/groups/g-99')
    })
  })

  describe('listMembers', () => {
    it('sends GET to /groups/{id}/members and returns items', async () => {
      const items = accessGenerator.groupMembers(2)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.listMembers('g-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/groups/g-1/members')
      expect(result).toEqual(items)
      expect(result).toHaveLength(2)
    })
  })

  describe('addMember', () => {
    it('sends POST to /groups/{id}/members with user_id', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(undefined)

      await api.addMember('g-1', 'user-1')

      expect(fetchModule.post).toHaveBeenCalledWith('/groups/g-1/members', {
        user_id: 'user-1',
      })
    })
  })

  describe('removeMember', () => {
    it('sends DELETE to /groups/{id}/members/{userId}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.removeMember('g-1', 'user-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/groups/g-1/members/user-1')
    })
  })

  describe('listUserGroups', () => {
    it('sends GET to /users/{id}/groups and returns items', async () => {
      const items = accessGenerator.groups(2)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.listUserGroups('user-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/users/user-1/groups')
      expect(result).toEqual(items)
    })
  })

  describe('createPermission', () => {
    it('sends POST to /permissions with snake_case body', async () => {
      const created = accessGenerator.permission()
      vi.mocked(fetchModule.post).mockResolvedValue(created)

      const result = await api.createPermission({ resource: 'board', action: 'write' })

      expect(fetchModule.post).toHaveBeenCalledWith('/permissions', {
        resource: 'board',
        action: 'write',
      })
      expect(result).toEqual(created)
    })

    it('sends target_id when provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(accessGenerator.permission())

      await api.createPermission({ resource: 'task', action: 'delete', targetId: 't-1' })

      expect(fetchModule.post).toHaveBeenCalledWith('/permissions', {
        resource: 'task',
        action: 'delete',
        target_id: 't-1',
      })
    })

    it('omits target_id when not provided', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(accessGenerator.permission())

      await api.createPermission({ resource: 'task', action: 'read' })

      const body = vi.mocked(fetchModule.post).mock.calls[0]![1] as Record<string, unknown>
      expect(body).not.toHaveProperty('target_id')
    })
  })

  describe('deletePermission', () => {
    it('sends DELETE to /permissions/{id}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.deletePermission('perm-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/permissions/perm-1')
    })
  })

  describe('findPermissions', () => {
    it('sends GET with resource query param', async () => {
      const items = accessGenerator.permissions(2)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.findPermissions('project')

      expect(fetchModule.get).toHaveBeenCalledWith('/permissions?resource=project')
      expect(result).toEqual(items)
    })

    it('adds target_id when provided', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ items: [], total: 0 })

      await api.findPermissions('project', 'p-1')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/permissions?resource=project&target_id=p-1',
      )
    })

    it('omits target_id when null', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ items: [], total: 0 })

      await api.findPermissions('project', null)

      expect(fetchModule.get).toHaveBeenCalledWith('/permissions?resource=project')
    })
  })

  describe('grantPermission', () => {
    it('sends POST to /groups/{id}/permissions', async () => {
      vi.mocked(fetchModule.post).mockResolvedValue(undefined)

      await api.grantPermission('g-1', 'perm-1')

      expect(fetchModule.post).toHaveBeenCalledWith('/groups/g-1/permissions', {
        permission_id: 'perm-1',
      })
    })
  })

  describe('revokePermission', () => {
    it('sends DELETE to /groups/{id}/permissions/{permId}', async () => {
      vi.mocked(fetchModule.del).mockResolvedValue(undefined)

      await api.revokePermission('g-1', 'perm-1')

      expect(fetchModule.del).toHaveBeenCalledWith('/groups/g-1/permissions/perm-1')
    })
  })

  describe('listGroupPermissions', () => {
    it('sends GET to /groups/{id}/permissions', async () => {
      const items = accessGenerator.permissions(3)
      vi.mocked(fetchModule.get).mockResolvedValue({ items, total: items.length })

      const result = await api.listGroupPermissions('g-1')

      expect(fetchModule.get).toHaveBeenCalledWith('/groups/g-1/permissions')
      expect(result).toEqual(items)
    })
  })

  describe('checkPermission', () => {
    it('sends GET with query params and returns check result', async () => {
      const check = accessGenerator.permissionCheck({ allowed: true })
      vi.mocked(fetchModule.get).mockResolvedValue(check)

      const result = await api.checkPermission('user-1', 'project', 'read')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/permissions/check?user_id=user-1&resource=project&action=read',
      )
      expect(result).toEqual(check)
    })

    it('includes target_id when provided', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue(accessGenerator.permissionCheck())

      await api.checkPermission('user-1', 'project', 'read', 'p-1')

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/permissions/check?user_id=user-1&resource=project&action=read&target_id=p-1',
      )
    })
  })
})
