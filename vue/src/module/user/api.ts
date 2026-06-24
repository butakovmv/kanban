import { get } from '../../fetch'

export interface UserDisplayInfo {
  id: string
  displayName: string
}

interface RawUserDisplayInfo {
  id: string
  display_name: string
}

function toUserDisplayInfo(raw: RawUserDisplayInfo): UserDisplayInfo {
  return {
    id: raw.id,
    displayName: raw.display_name,
  }
}

export async function listUsers(ids: string[]): Promise<UserDisplayInfo[]> {
  if (ids.length === 0) return []
  const uniqueIds = [...new Set(ids)]
  const params = new URLSearchParams()
  params.set('ids', uniqueIds.join(','))
  const data: { users: RawUserDisplayInfo[] } = await get(`/users?${params}`)
  return data.users.map(toUserDisplayInfo)
}
