import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as accessApi from './api'

export const useAccessStore = defineStore('access', () => {
  const groups = ref<accessApi.Group[]>([])
  const currentGroup = ref<accessApi.Group | null>(null)
  const members = ref<accessApi.GroupMember[]>([])
  const permissions = ref<accessApi.Permission[]>([])
  const groupPermissions = ref<accessApi.Permission[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadGroups(): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      groups.value = await accessApi.listGroups()
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load groups'
      groups.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadGroup(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      currentGroup.value = await accessApi.getGroup(id)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load group'
      currentGroup.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  async function createGroup(
    req: accessApi.CreateGroupRequest,
  ): Promise<accessApi.Group | null> {
    loading.value = true
    error.value = null
    try {
      const group = await accessApi.createGroup(req)
      groups.value = [...groups.value, group]
      return group
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to create group'
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateGroup(
    id: string,
    req: accessApi.UpdateGroupRequest,
  ): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      const updated = await accessApi.updateGroup(id, req)
      groups.value = groups.value.map((g) => (g.id === id ? updated : g))
      if (currentGroup.value !== null && currentGroup.value.id === id) {
        currentGroup.value = updated
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to update group'
      return false
    } finally {
      loading.value = false
    }
  }

  async function deleteGroup(id: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await accessApi.deleteGroup(id)
      groups.value = groups.value.filter((g) => g.id !== id)
      if (currentGroup.value !== null && currentGroup.value.id === id) {
        currentGroup.value = null
        members.value = []
        groupPermissions.value = []
      }
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to delete group'
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadMembers(groupId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      members.value = await accessApi.listMembers(groupId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load members'
      members.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function addMember(groupId: string, userId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await accessApi.addMember(groupId, userId)
      await loadMembers(groupId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to add member'
      return false
    } finally {
      loading.value = false
    }
  }

  async function removeMember(groupId: string, userId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await accessApi.removeMember(groupId, userId)
      members.value = members.value.filter((m) => m.userId !== userId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to remove member'
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadPermissions(): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      permissions.value = await accessApi.findPermissions('')
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load permissions'
      permissions.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadGroupPermissions(groupId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      groupPermissions.value = await accessApi.listGroupPermissions(groupId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load group permissions'
      groupPermissions.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function grantPermission(groupId: string, permissionId: string): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await accessApi.grantPermission(groupId, permissionId)
      await loadGroupPermissions(groupId)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to grant permission'
      return false
    } finally {
      loading.value = false
    }
  }

  async function revokePermission(
    groupId: string,
    permissionId: string,
  ): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      await accessApi.revokePermission(groupId, permissionId)
      groupPermissions.value = groupPermissions.value.filter(
        (p) => p.id !== permissionId,
      )
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to revoke permission'
      return false
    } finally {
      loading.value = false
    }
  }

  async function checkPermission(
    userId: string,
    resource: string,
    action: string,
    targetId?: string | null,
  ): Promise<accessApi.PermissionCheck | null> {
    loading.value = true
    error.value = null
    try {
      return await accessApi.checkPermission(userId, resource, action, targetId)
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to check permission'
      return null
    } finally {
      loading.value = false
    }
  }

  function clearCurrent(): void {
    currentGroup.value = null
    members.value = []
    groupPermissions.value = []
  }

  return {
    groups,
    currentGroup,
    members,
    permissions,
    groupPermissions,
    loading,
    error,
    loadGroups,
    loadGroup,
    createGroup,
    updateGroup,
    deleteGroup,
    loadMembers,
    addMember,
    removeMember,
    loadPermissions,
    loadGroupPermissions,
    grantPermission,
    revokePermission,
    checkPermission,
    clearCurrent,
  }
})
