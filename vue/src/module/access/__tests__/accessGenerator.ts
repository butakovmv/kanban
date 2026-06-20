import type {
  Group,
  GroupMember,
  Permission,
  CreateGroupRequest,
  UpdateGroupRequest,
  CreatePermissionRequest,
  PermissionCheck,
} from '../api'

export const accessGenerator = {
  group(overrides: Partial<Group> = {}): Group {
    const id = `group-${Math.random().toString(36).slice(2, 10)}`
    const name = `Group ${Math.random().toString(36).slice(2, 6)}`
    return {
      id,
      name,
      description: `Description for ${name}`,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  groups(count: number): Group[] {
    return Array.from({ length: count }, () => this.group())
  },

  groupMember(overrides: Partial<GroupMember> = {}): GroupMember {
    return {
      groupId: `group-${Math.random().toString(36).slice(2, 10)}`,
      userId: `user-${Math.random().toString(36).slice(2, 8)}`,
      addedAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  groupMembers(count: number): GroupMember[] {
    return Array.from({ length: count }, () => this.groupMember())
  },

  permission(overrides: Partial<Permission> = {}): Permission {
    return {
      id: `perm-${Math.random().toString(36).slice(2, 10)}`,
      resource: 'project',
      action: 'read',
      targetId: null,
      createdAt: new Date('2025-01-01T00:00:00Z').toISOString(),
      ...overrides,
    }
  },

  permissions(count: number): Permission[] {
    return Array.from({ length: count }, () => this.permission())
  },

  createGroupRequest(
    overrides: Partial<CreateGroupRequest> = {},
  ): CreateGroupRequest {
    return {
      name: `Group ${Math.random().toString(36).slice(2, 6)}`,
      description: 'Test description',
      ...overrides,
    }
  },

  updateGroupRequest(
    overrides: Partial<UpdateGroupRequest> = {},
  ): UpdateGroupRequest {
    return {
      name: `Updated ${Math.random().toString(36).slice(2, 6)}`,
      ...overrides,
    }
  },

  createPermissionRequest(
    overrides: Partial<CreatePermissionRequest> = {},
  ): CreatePermissionRequest {
    return {
      resource: 'project',
      action: 'read',
      ...overrides,
    }
  },

  permissionCheck(overrides: Partial<PermissionCheck> = {}): PermissionCheck {
    return {
      allowed: true,
      reason: null,
      ...overrides,
    }
  },
}
