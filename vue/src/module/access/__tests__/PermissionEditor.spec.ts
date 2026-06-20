import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import PermissionEditor from '../PermissionEditor.vue'
import * as api from '../api'
import { accessGenerator } from './accessGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    listGroupPermissions: vi.fn(),
    grantPermission: vi.fn(),
    revokePermission: vi.fn(),
  }
})

describe('PermissionEditor', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])
  })

  it('renders the title', async () => {
    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()

    expect(wrapper.find('h3').text()).toBe('Разрешения группы')
  })

  it('loads permissions on mount', async () => {
    mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()

    expect(api.listGroupPermissions).toHaveBeenCalledWith('g-1')
  })

  it('renders permission rows from the store', async () => {
    const perms = accessGenerator.permissions(2)
    vi.mocked(api.listGroupPermissions).mockResolvedValue(perms)

    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()
    await nextTick()

    const rows = wrapper.findAll('tbody tr')
    expect(rows).toHaveLength(2)
  })

  it('shows empty state when no permissions', async () => {
    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()
    await nextTick()

    expect(wrapper.find('.permission-editor__empty').exists()).toBe(true)
    expect(wrapper.find('.permission-editor__empty').text()).toBe(
      'Разрешения не назначены',
    )
  })

  it('calls grantPermission with the permission id on grant', async () => {
    vi.mocked(api.grantPermission).mockResolvedValue(undefined)
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])

    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()

    await wrapper.find('.permission-editor__grant-form button').trigger('click')
    await flushPromises()
    await nextTick()

    expect(api.grantPermission).toHaveBeenCalledWith('g-1', 'project:read')
  })

  it('calls revokePermission with the permission id on revoke', async () => {
    const perm = accessGenerator.permission({ id: 'perm-1' })
    vi.mocked(api.listGroupPermissions).mockResolvedValue([perm])
    vi.mocked(api.revokePermission).mockResolvedValue(undefined)

    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()
    await nextTick()

    await wrapper.find('.permission-editor__revoke-btn').trigger('click')
    await flushPromises()
    await nextTick()

    expect(api.revokePermission).toHaveBeenCalledWith('g-1', 'perm-1')
  })

  it('has resource and action dropdowns', async () => {
    const wrapper = mount(PermissionEditor, {
      props: { groupId: 'g-1' },
    })
    await flushPromises()

    const selects = wrapper.findAll('select')
    expect(selects).toHaveLength(2)
  })
})
