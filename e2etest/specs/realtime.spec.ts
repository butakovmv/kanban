import { test, expect } from '../fixtures'
import {
  loginAsNewUser,
  createProjectViaUi,
  createBoardViaApi,
} from '../helpers'

test.describe('Realtime SSE sync', () => {
  test.skip('should sync task creation across two browser contexts @smoke @critical', () => {
    // Пропущено: требуется реализация кросс-пользовательского доступа к доске (sharing).
    // На данный момент пользователь rt2 не имеет доступа к доске пользователя rt1.
  })
})
