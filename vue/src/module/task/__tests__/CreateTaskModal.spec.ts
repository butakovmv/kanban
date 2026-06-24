import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import CreateTaskModal from '../CreateTaskModal.vue'

describe('CreateTaskModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('emits submit with the typed values when the form is valid', async () => {
    const wrapper = mount(CreateTaskModal, {
      props: {
        projectId: 'b-1',
        columnId: 'c-1',
      },
    })

    await wrapper.find('input[type="text"]').setValue('New task')
    await wrapper.find('textarea').setValue('Description text')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')?.[0]).toEqual([
      {
        projectId: 'b-1',
        columnId: 'c-1',
        title: 'New task',
        description: 'Description text',
      },
    ])
  })

  it('omits description in the emitted payload when the textarea is empty', async () => {
    const wrapper = mount(CreateTaskModal, {
      props: { projectId: 'b-1', columnId: 'c-1' },
    })

    await wrapper.find('input[type="text"]').setValue('Title only')
    await wrapper.find('form').trigger('submit')

    const payload = wrapper.emitted('submit')?.[0]?.[0] as Record<string, unknown>
    expect(payload['title']).toBe('Title only')
    expect(payload).not.toHaveProperty('description')
  })

  it('emits cancel when the cancel button is clicked', async () => {
    const wrapper = mount(CreateTaskModal, {
      props: { projectId: 'b-1', columnId: 'c-1' },
    })

    const buttons = wrapper.findAll('button')
    const cancel = buttons.find((b) => b.text() === 'Cancel')
    expect(cancel).toBeDefined()
    await cancel!.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('disables the submit button when the title is empty', async () => {
    const wrapper = mount(CreateTaskModal, {
      props: { projectId: 'b-1', columnId: 'c-1' },
    })

    const submit = wrapper.find('button[type="submit"]')
    expect((submit.element as HTMLButtonElement).disabled).toBe(true)
  })
})
