import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'
import DocumentUpload from '../DocumentUpload.vue'

/**
 * Декодирует base64 в строку через браузерный `atob`.
 * @param value base64
 * @returns исходная строка
 */
function fromBase64(value: string): string {
  return decodeURIComponent(escape(atob(value)))
}

describe('DocumentUpload', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('emits submit with base64 content, fileName, contentType and metadata when valid', async () => {
    const wrapper = mount(DocumentUpload, {
      props: { projectId: 'p-1', uploadedBy: 'u-1' },
    })

    await wrapper.find('input[type="text"]').setValue('My document')
    await wrapper.find('textarea').setValue('A short description')
    await nextTick()

    const file = new File(['hello world'], 'hello.txt', { type: 'text/plain' })
    const fileInput = wrapper.find('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      writable: false,
    })
    await fileInput.trigger('change')
    await flushPromises()
    await new Promise((resolve) => setTimeout(resolve, 0))
    await nextTick()

    await wrapper.find('form').trigger('submit')
    await nextTick()

    const emitted = wrapper.emitted('submit')
    expect(emitted).toBeTruthy()
    const payload = emitted?.[0]?.[0] as Record<string, unknown>
    expect(payload['projectId']).toBe('p-1')
    expect(payload['uploadedBy']).toBe('u-1')
    expect(payload['title']).toBe('My document')
    expect(payload['description']).toBe('A short description')
    expect(payload['fileName']).toBe('hello.txt')
    expect(payload['contentType']).toBe('text/plain')
    expect(typeof payload['contentBase64']).toBe('string')
    expect(fromBase64(payload['contentBase64'] as string)).toBe('hello world')
  })

  it('omits description in the emitted payload when the textarea is empty', async () => {
    const wrapper = mount(DocumentUpload, {
      props: { projectId: 'p-1', uploadedBy: 'u-1' },
    })

    await wrapper.find('input[type="text"]').setValue('Title only')
    await nextTick()

    const file = new File(['x'], 'x.txt', { type: 'text/plain' })
    const fileInput = wrapper.find('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      writable: false,
    })
    await fileInput.trigger('change')
    await flushPromises()
    await new Promise((resolve) => setTimeout(resolve, 0))
    await nextTick()

    await wrapper.find('form').trigger('submit')
    await nextTick()

    const payload = wrapper.emitted('submit')?.[0]?.[0] as Record<string, unknown>
    expect(payload['title']).toBe('Title only')
    expect(payload).not.toHaveProperty('description')
  })

  it('emits cancel when the cancel button is clicked', async () => {
    const wrapper = mount(DocumentUpload, {
      props: { projectId: 'p-1', uploadedBy: 'u-1' },
    })

    const buttons = wrapper.findAll('button')
    const cancel = buttons.find((b) => b.text() === 'Cancel')
    expect(cancel).toBeDefined()
    await cancel!.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('disables the submit button when title is empty', async () => {
    const wrapper = mount(DocumentUpload, {
      props: { projectId: 'p-1', uploadedBy: 'u-1' },
    })

    const submit = wrapper.find('button[type="submit"]')
    expect((submit.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('disables the submit button until a file is selected', async () => {
    const wrapper = mount(DocumentUpload, {
      props: { projectId: 'p-1', uploadedBy: 'u-1' },
    })

    await wrapper.find('input[type="text"]').setValue('Title')
    await nextTick()

    const submit = wrapper.find('button[type="submit"]')
    expect((submit.element as HTMLButtonElement).disabled).toBe(true)
  })
})
