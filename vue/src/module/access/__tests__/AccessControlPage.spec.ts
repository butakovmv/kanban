import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import AccessControlPage from '../AccessControlPage.vue'
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
    listGroupPermissions: vi.fn(),
    grantPermission: vi.fn(),
    revokePermission: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: '/access',
        name: 'access-control',
        component: AccessControlPage,
      },
    ],
  })
}

describe('AccessControlPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(api.listGroups).mockResolvedValue([])
    vi.mocked(api.listMembers).mockResolvedValue([])
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])
  })

  it('renders the page title', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Управление доступом')
  })

  it('renders groups from the store', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })

    await flushPromises()

    const store = useAccessStore()
    store.groups = accessGenerator.groups(2)
    await nextTick()

    const items = wrapper.findAll('.access-control__group-item')
    expect(items).toHaveLength(2)
  })

  it('toggles create form on button click', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    expect(wrapper.find('.access-control__form').exists()).toBe(false)

    await wrapper.find('.access-control__create-btn').trigger('click')
    await nextTick()

    expect(wrapper.find('.access-control__form').exists()).toBe(true)
  })

  it('creates a group via the form', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const created = accessGenerator.group({ name: 'Test Group' })
    vi.mocked(api.createGroup).mockResolvedValue(created)

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    await wrapper.find('.access-control__create-btn').trigger('click')
    await nextTick()
    await wrapper.find('input[type="text"]').setValue('Test Group')
    await nextTick()
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(api.createGroup).toHaveBeenCalledWith({
      name: 'Test Group',
      description: null,
    })
  })

  it('selects a group and shows details', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const group = accessGenerator.group({ id: 'g-1', name: 'Selected Group' })
    vi.mocked(api.getGroup).mockResolvedValue(group)
    vi.mocked(api.listMembers).mockResolvedValue([])
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    const store = useAccessStore()
    store.groups = [group]
    await nextTick()

    await wrapper.find('.access-control__group-btn').trigger('click')
    await flushPromises()
    await nextTick()

    expect(wrapper.text()).toContain('Selected Group')
  })

  it('shows placeholder when no group selected', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    expect(wrapper.find('.access-control__placeholder').exists()).toBe(true)
    expect(wrapper.find('.access-control__placeholder').text()).toBe(
      'Выберите группу для управления',
    )
  })

  it('shows empty state when no groups', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    expect(wrapper.find('.access-control__empty').exists()).toBe(true)
  })

  it('adds a member to selected group', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const group = accessGenerator.group({ id: 'g-1' })
    vi.mocked(api.getGroup).mockResolvedValue(group)
    vi.mocked(api.listMembers).mockResolvedValue([])
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])
    vi.mocked(api.addMember).mockResolvedValue(undefined)

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    const store = useAccessStore()
    store.groups = [group]
    store.currentGroup = group
    await nextTick()
    await wrapper.find('.access-control__group-btn').trigger('click')
    await flushPromises()
    await nextTick()

    const input = wrapper.find('.access-control__add-member input')
    await input.setValue('user-42')
    await nextTick()
    await wrapper.find('.access-control__add-member button').trigger('click')
    await flushPromises()
    await nextTick()
    await nextTick()

    expect(api.addMember).toHaveBeenCalledWith('g-1', 'user-42')
  })

  it('removes a member from selected group', async () => {
    const router = createTestRouter()
    await router.push('/access')
    await router.isReady()

    const group = accessGenerator.group({ id: 'g-1' })
    vi.mocked(api.getGroup).mockResolvedValue(group)
    vi.mocked(api.listMembers).mockResolvedValue([accessGenerator.groupMember({ groupId: 'g-1', userId: 'user-1' })])
    vi.mocked(api.listGroupPermissions).mockResolvedValue([])
    vi.mocked(api.removeMember).mockResolvedValue(undefined)

    const wrapper = mount(AccessControlPage, {
      global: { plugins: [router] },
    })
    await flushPromises()

    const store = useAccessStore()
    store.groups = [group]
    store.currentGroup = group
    store.members = [accessGenerator.groupMember({ groupId: 'g-1', userId: 'user-1' })]
    await nextTick()

    await wrapper.find('.access-control__member-item button').trigger('click')
    await flushPromises()
    await nextTick()

    expect(api.removeMember).toHaveBeenCalledWith('g-1', 'user-1')
  })
})
