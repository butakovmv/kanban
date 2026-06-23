import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as api from '../api'
import * as fetchModule from '../../../fetch'

vi.mock('../../../fetch', async () => {
  const actual = await vi.importActual<typeof fetchModule>('../../../fetch')
  return {
    ...actual,
    get: vi.fn(),
  }
})

describe('report api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getCfd', () => {
    it('sends GET to /reports/cfd with query params', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ points: [] })

      const result = await api.getCfd({
        from: '2025-01-01',
        to: '2025-01-31',
        interval: 'DAY',
      })

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/reports/cfd?from=2025-01-01&to=2025-01-31&interval=DAY',
      )
      expect(result).toEqual([])
    })

    it('includes optional project_id and board_id', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ points: [] })

      await api.getCfd({
        projectId: 'proj-1',
        boardId: 'board-2',
        from: '2025-01-01',
        to: '2025-01-31',
        interval: 'WEEK',
      })

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/reports/cfd?from=2025-01-01&to=2025-01-31&interval=WEEK&project_id=proj-1&board_id=board-2',
      )
    })
  })

  describe('getLeadTime', () => {
    it('sends GET to /reports/lead-time with query params', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ points: [] })

      const result = await api.getLeadTime({
        from: '2025-01-01',
        to: '2025-01-31',
      })

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/reports/lead-time?from=2025-01-01&to=2025-01-31',
      )
      expect(result).toEqual([])
    })

    it('includes optional project_id', async () => {
      vi.mocked(fetchModule.get).mockResolvedValue({ points: [] })

      await api.getLeadTime({
        projectId: 'proj-1',
        from: '2025-01-01',
        to: '2025-01-31',
      })

      expect(fetchModule.get).toHaveBeenCalledWith(
        '/reports/lead-time?from=2025-01-01&to=2025-01-31&project_id=proj-1',
      )
    })
  })
})
