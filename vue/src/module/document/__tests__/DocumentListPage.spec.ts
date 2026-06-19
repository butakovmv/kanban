import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { nextTick } from 'vue'
import { setActivePinia, createPinia } from 'pinia'
import ProjectListPage from '../../project/ProjectListPage.vue'
import ProjectSettingsPage from '../../project/ProjectSettingsPage.vue'
import DocumentListPage from '../DocumentListPage.vue'
import * as api from '../api'
import { documentGenerator } from './documentGenerator'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof import('../api')>('../api')
  return {
    ...actual,
    listDocuments: vi.fn(),
    getDocument: vi.fn(),
    createDocument: vi.fn(),
    updateDocument: vi.fn(),
    replaceDocumentContent: vi.fn(),
    deleteDocument: vi.fn(),
    getDocumentDownloadUrl: vi.fn(),
  }
})

function createTestRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/projects', name: 'projects', component: ProjectListPage },
      {
        path: '/projects/:id',
        name: 'project-settings',
        component: ProjectSettingsPage,
      },
      {
        path: '/projects/:id/documents',
        name: 'project-documents',
        component: DocumentListPage,
      },
    ],
  })
}

describe('DocumentListPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(api.listDocuments).mockResolvedValue([])
    vi.spyOn(window, 'confirm').mockReturnValue(false)
    vi.spyOn(window, 'open').mockImplementation(() => null)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads documents for the route project and renders a row per document', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/documents')
    await router.isReady()

    const documents = documentGenerator.documents(2, 'p-1')
    vi.mocked(api.listDocuments).mockResolvedValue(documents)

    const wrapper = mount(DocumentListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(api.listDocuments).toHaveBeenCalledWith('p-1')
    const rows = wrapper.findAll('.document-list__row')
    expect(rows).toHaveLength(2)
    expect(wrapper.find('h1').text()).toBe('Documents')
  })

  it('opens DocumentUpload modal and calls createDocument on submit', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/documents')
    await router.isReady()

    const created = documentGenerator.document({ id: 'd-new' })
    vi.mocked(api.createDocument).mockResolvedValue(created)

    const wrapper = mount(DocumentListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    await wrapper.find('.document-list__upload-btn').trigger('click')
    await nextTick()

    expect(wrapper.find('.upload').exists()).toBe(true)

    await wrapper.find('input[type="text"]').setValue('My file')
    await wrapper.find('textarea').setValue('My description')
    await nextTick()

    const fileInput = wrapper.find('input[type="file"]')
    const file = new File(['hello'], 'hello.txt', { type: 'text/plain' })
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      writable: false,
    })
    await fileInput.trigger('change')
    await flushPromises()
    await new Promise((resolve) => setTimeout(resolve, 0))
    await nextTick()

    await wrapper.find('form').trigger('submit')
    await flushPromises()
    await nextTick()

    expect(api.createDocument).toHaveBeenCalledTimes(1)
    const callArgs = vi.mocked(api.createDocument).mock.calls[0]?.[0]
    expect(callArgs?.projectId).toBe('p-1')
    expect(callArgs?.title).toBe('My file')
    expect(callArgs?.description).toBe('My description')
    expect(callArgs?.fileName).toBe('hello.txt')
    expect(callArgs?.contentType).toBe('text/plain')
    expect(typeof callArgs?.contentBase64).toBe('string')

    expect(wrapper.find('.upload').exists()).toBe(false)
  })

  it('opens presigned URL via window.open when downloading a document', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/documents')
    await router.isReady()

    const document = documentGenerator.document({ id: 'd-1', fileName: 'a.txt' })
    vi.mocked(api.listDocuments).mockResolvedValue([document])
    vi.mocked(api.getDocumentDownloadUrl).mockResolvedValue({
      url: '/api/v1/documents/d-1/raw?sig=abc',
    })

    const wrapper = mount(DocumentListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    const downloadBtn = wrapper
      .findAll('.document-list__action')
      .find((b) => b.text() === 'Download')
    expect(downloadBtn).toBeDefined()
    await downloadBtn!.trigger('click')
    await flushPromises()

    expect(api.getDocumentDownloadUrl).toHaveBeenCalledWith('d-1')
    expect(window.open).toHaveBeenCalledWith(
      '/api/v1/documents/d-1/raw?sig=abc',
      '_blank',
      'noopener,noreferrer',
    )
  })

  it('renders an empty state when there are no documents', async () => {
    const router = createTestRouter()
    await router.push('/projects/p-1/documents')
    await router.isReady()

    const wrapper = mount(DocumentListPage, {
      global: { plugins: [router] },
    })

    await flushPromises()
    await nextTick()

    expect(wrapper.find('.document-list__empty').exists()).toBe(true)
  })
})
