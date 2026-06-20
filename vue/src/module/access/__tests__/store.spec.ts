import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAccessStore } from '../store'
import * as api from '../api'
import { accessGenerator } from './accessGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    listGroups: vi.fn(),
    getGroup: vi.fn(),
    createGroup: vi.fn(),
    updateGroup: vi.fn(),
    deleteGroup: vi.fn(),
    listMembers: vi.fn(),
    addMember: vi.fn(),
    removeMember: vi.fn(),
    listUserGroups: vi.fn(),
    createPermission: vi.fn(),
    deletePermission: vi.fn(),
    findPermissions: vi.fn(),
    grantPermission: vi.fn(),
    revokePermission: vi.fn(),
    listGroupPermissions: vi.fn(),
    checkPermission: vi.fn(),
  }
})

describe('access store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useAccessStore()
    expect(store.groups).toEqual([])
    expect(store.currentGroup).toBeNull()
    expect(store.members).toEqual([])
    expect(store.permissions).toEqual([])
    expect(store.groupPermissions).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('loadGroups', () => {
    it('populates groups on success', async () => {
      const groups = accessGenerator.groups(3)
      vi.mocked(api.listGroups).mockResolvedValue(groups)

      const store = useAccessStore()
      const success = await store.loadGroups()

      expect(success).toBe(true)
      expect(store.groups).toEqual(groups)
      expect(api.listGroups).toHaveBeenCalled()
    })

    it('sets error and clears groups on failure', async () => {
      vi.mocked(api.listGroups).mockRejectedValue(new Error('Network error'))

      const store = useAccessStore()
      const success = await store.loadGroups()

      expect(success).toBe(false)
      expect(store.error).toBe('Network error')
      expect(store.groups).toEqual([])
    })
  })

  describe('loadGroup', () => {
    it('populates currentGroup on success', async () => {
      const group = accessGenerator.group({ id: 'g-1' })
      vi.mocked(api.getGroup).mockResolvedValue(group)

      const store = useAccessStore()
      const success = await store.loadGroup('g-1')

      expect(success).toBe(true)
      expect(store.currentGroup).toEqual(group)
    })

    it('sets error and clears currentGroup on failure', async () => {
      vi.mocked(api.getGroup).mockRejectedValue(new Error('Not found'))

      const store = useAccessStore()
      const success = await store.loadGroup('g-1')

      expect(success).toBe(false)
      expect(store.error).toBe('Not found')
      expect(store.currentGroup).toBeNull()
    })
  })

  describe('createGroup', () => {
    it('appends new group and returns it on success', async () => {
      const existing = accessGenerator.group({ id: 'g-1' })
      const created = accessGenerator.group({ id: 'g-2' })
      vi.mocked(api.createGroup).mockResolvedValue(created)

      const store = useAccessStore()
      store.groups = [existing]

      const result = await store.createGroup(accessGenerator.createGroupRequest())

      expect(result).toEqual(created)
      expect(store.groups).toEqual([existing, created])
    })

    it('sets error and returns null on failure', async () => {
      vi.mocked(api.createGroup).mockRejectedValue(new Error('Validation failed'))

      const store = useAccessStore()
      const result = await store.createGroup(accessGenerator.createGroupRequest())

      expect(result).toBeNull()
      expect(store.error).toBe('Validation failed')
    })
  })

  describe('updateGroup', () => {
    it('replaces the group in the list and returns true', async () => {
      const original = accessGenerator.group({ id: 'g-1', name: 'Old' })
      const updated = accessGenerator.group({ id: 'g-1', name: 'New' })
      vi.mocked(api.updateGroup).mockResolvedValue(updated)

      const store = useAccessStore()
      store.groups = [original]

      const success = await store.updateGroup('g-1', { name: 'New' })

      expect(success).toBe(true)
      expect(store.groups).toEqual([updated])
    })

    it('updates currentGroup when it matches', async () => {
      const updated = accessGenerator.group({ id: 'g-1', name: 'Renamed' })
      vi.mocked(api.updateGroup).mockResolvedValue(updated)

      const store = useAccessStore()
      store.currentGroup = accessGenerator.group({ id: 'g-1', name: 'Old' })

      await store.updateGroup('g-1', { name: 'Renamed' })

      expect(store.currentGroup).toEqual(updated)
    })
  })

  describe('deleteGroup', () => {
    it('removes the group from the list and returns true', async () => {
      const group = accessGenerator.group({ id: 'g-1' })
      vi.mocked(api.deleteGroup).mockResolvedValue(undefined)

      const store = useAccessStore()
      store.groups = [group]

      const success = await store.deleteGroup('g-1')

      expect(success).toBe(true)
      expect(store.groups).toEqual([])
    })

    it('clears currentGroup and members when matching', async () => {
      vi.mocked(api.deleteGroup).mockResolvedValue(undefined)

      const store = useAccessStore()
      store.currentGroup = accessGenerator.group({ id: 'g-1' })
      store.members = accessGenerator.groupMembers(2)
      store.groupPermissions = accessGenerator.permissions(1)

      await store.deleteGroup('g-1')

      expect(store.currentGroup).toBeNull()
      expect(store.members).toEqual([])
      expect(store.groupPermissions).toEqual([])
    })
  })

  describe('loadMembers', () => {
    it('populates members on success', async () => {
      const members = accessGenerator.groupMembers(2)
      vi.mocked(api.listMembers).mockResolvedValue(members)

      const store = useAccessStore()
      const success = await store.loadMembers('g-1')

      expect(success).toBe(true)
      expect(store.members).toEqual(members)
    })
  })

  describe('addMember', () => {
    it('calls api and reloads members on success', async () => {
      vi.mocked(api.addMember).mockResolvedValue(undefined)
      vi.mocked(api.listMembers).mockResolvedValue([])

      const store = useAccessStore()
      const success = await store.addMember('g-1', 'user-1')

      expect(success).toBe(true)
      expect(api.addMember).toHaveBeenCalledWith('g-1', 'user-1')
      expect(api.listMembers).toHaveBeenCalledWith('g-1')
    })
  })

  describe('removeMember', () => {
    it('calls api and removes member from local state', async () => {
      vi.mocked(api.removeMember).mockResolvedValue(undefined)

      const store = useAccessStore()
      store.members = accessGenerator.groupMembers(2)

      const success = await store.removeMember('g-1', store.members[0].userId)

      expect(success).toBe(true)
      expect(store.members).toHaveLength(1)
    })
  })

  describe('loadGroupPermissions', () => {
    it('populates groupPermissions on success', async () => {
      const perms = accessGenerator.permissions(2)
      vi.mocked(api.listGroupPermissions).mockResolvedValue(perms)

      const store = useAccessStore()
      const success = await store.loadGroupPermissions('g-1')

      expect(success).toBe(true)
      expect(store.groupPermissions).toEqual(perms)
    })
  })

  describe('grantPermission', () => {
    it('calls api and reloads permissions on success', async () => {
      vi.mocked(api.grantPermission).mockResolvedValue(undefined)
      vi.mocked(api.listGroupPermissions).mockResolvedValue([])

      const store = useAccessStore()
      const success = await store.grantPermission('g-1', 'perm-1')

      expect(success).toBe(true)
      expect(api.grantPermission).toHaveBeenCalledWith('g-1', 'perm-1')
      expect(api.listGroupPermissions).toHaveBeenCalledWith('g-1')
    })
  })

  describe('revokePermission', () => {
    it('calls api and removes from local state', async () => {
      vi.mocked(api.revokePermission).mockResolvedValue(undefined)

      const store = useAccessStore()
      store.groupPermissions = accessGenerator.permissions(2)

      const success = await store.revokePermission('g-1', store.groupPermissions[0].id)

      expect(success).toBe(true)
      expect(store.groupPermissions).toHaveLength(1)
    })
  })

  describe('checkPermission', () => {
    it('returns check result on success', async () => {
      const check = accessGenerator.permissionCheck({ allowed: true })
      vi.mocked(api.checkPermission).mockResolvedValue(check)

      const store = useAccessStore()
      const result = await store.checkPermission('user-1', 'project', 'read')

      expect(result).toEqual(check)
    })

    it('returns null on failure', async () => {
      vi.mocked(api.checkPermission).mockRejectedValue(new Error('Server error'))

      const store = useAccessStore()
      const result = await store.checkPermission('user-1', 'project', 'read')

      expect(result).toBeNull()
      expect(store.error).toBe('Server error')
    })
  })

  describe('clearCurrent', () => {
    it('resets currentGroup, members, groupPermissions', () => {
      const store = useAccessStore()
      store.currentGroup = accessGenerator.group()
      store.members = accessGenerator.groupMembers(1)
      store.groupPermissions = accessGenerator.permissions(1)

      store.clearCurrent()

      expect(store.currentGroup).toBeNull()
      expect(store.members).toEqual([])
      expect(store.groupPermissions).toEqual([])
    })
  })
})
