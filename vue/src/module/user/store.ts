import { ref } from 'vue'
import { defineStore } from 'pinia'
import { listUsers } from './api'

export const useUserStore = defineStore('user', () => {
  const displayNames = ref<Record<string, string>>({})

  async function ensureUsers(userIds: string[]) {
    const missing = userIds.filter((id) => !displayNames.value[id])
    if (missing.length === 0) return
    try {
      const users = await listUsers(missing)
      for (const user of users) {
        displayNames.value[user.id] = user.displayName
      }
    } catch {
      /* ignore fetch errors */
    }
  }

  function getDisplayName(userId: string): string | undefined {
    return displayNames.value[userId]
  }

  return { displayNames, ensureUsers, getDisplayName }
})
