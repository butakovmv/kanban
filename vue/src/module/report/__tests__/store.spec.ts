import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useReportStore } from '../store'
import * as api from '../api'

vi.mock('../api', async () => {
  const actual = await vi.importActual<typeof api>('../api')
  return {
    ...actual,
    getCfd: vi.fn(),
    getLeadTime: vi.fn(),
  }
})

describe('report store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts with empty state', () => {
    const store = useReportStore()
    expect(store.cfdData).toEqual([])
    expect(store.leadTimeData).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('loadCfd', () => {
    it('populates cfdData on success', async () => {
      const data = [
        { date: '2025-01-01', columnId: 'c1', columnName: 'To Do', count: 5 },
      ]
      vi.mocked(api.getCfd).mockResolvedValue(data)

      const store = useReportStore()
      const success = await store.loadCfd({
        from: '2025-01-01',
        to: '2025-01-31',
        interval: 'DAY',
      })

      expect(success).toBe(true)
      expect(store.cfdData).toEqual(data)
      expect(api.getCfd).toHaveBeenCalledWith({
        from: '2025-01-01',
        to: '2025-01-31',
        interval: 'DAY',
      })
    })

    it('sets error on failure', async () => {
      vi.mocked(api.getCfd).mockRejectedValue(new Error('API error'))

      const store = useReportStore()
      const success = await store.loadCfd({
        from: '2025-01-01',
        to: '2025-01-31',
        interval: 'DAY',
      })

      expect(success).toBe(false)
      expect(store.error).toBe('API error')
      expect(store.cfdData).toEqual([])
    })

    it('uses generic error message for non-Error rejection', async () => {
      vi.mocked(api.getCfd).mockRejectedValue('plain string')

      const store = useReportStore()
      await store.loadCfd({ from: '', to: '', interval: 'DAY' })

      expect(store.error).toBe('Failed to load CFD data')
    })
  })

  describe('loadLeadTime', () => {
    it('populates leadTimeData on success', async () => {
      const data = [
        { date: '2025-01-15', taskId: 't-1', taskTitle: 'Bug fix', leadTimeHours: 12 },
      ]
      vi.mocked(api.getLeadTime).mockResolvedValue(data)

      const store = useReportStore()
      const success = await store.loadLeadTime({
        from: '2025-01-01',
        to: '2025-01-31',
      })

      expect(success).toBe(true)
      expect(store.leadTimeData).toEqual(data)
    })

    it('sets error on failure', async () => {
      vi.mocked(api.getLeadTime).mockRejectedValue(new Error('Not found'))

      const store = useReportStore()
      const success = await store.loadLeadTime({
        from: '2025-01-01',
        to: '2025-01-31',
      })

      expect(success).toBe(false)
      expect(store.error).toBe('Not found')
      expect(store.leadTimeData).toEqual([])
    })
  })
})
