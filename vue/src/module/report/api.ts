import { get } from '../../fetch'

export interface CfdDataPoint {
  date: string
  columnId: string
  columnName: string
  count: number
}

export interface LeadTimeDataPoint {
  date: string
  taskId: string
  taskTitle: string
  leadTimeHours: number
}

export interface CfdParams {
  projectId?: string
  boardId?: string
  from: string
  to: string
  interval: 'DAY' | 'WEEK' | 'MONTH'
}

export interface LeadTimeParams {
  projectId?: string
  from: string
  to: string
}

function cfdParamsToQuery(params: CfdParams): string {
  const q: Record<string, string> = {
    from: params.from,
    to: params.to,
    interval: params.interval,
  }
  if (params.projectId !== undefined) q['project_id'] = params.projectId
  if (params.boardId !== undefined) q['board_id'] = params.boardId
  return new URLSearchParams(q).toString()
}

function leadTimeParamsToQuery(params: LeadTimeParams): string {
  const q: Record<string, string> = {
    from: params.from,
    to: params.to,
  }
  if (params.projectId !== undefined) q['project_id'] = params.projectId
  return new URLSearchParams(q).toString()
}

export function getCfd(params: CfdParams): Promise<CfdDataPoint[]> {
  return get<CfdDataPoint[]>(`/reports/cfd?${cfdParamsToQuery(params)}`)
}

export function getLeadTime(params: LeadTimeParams): Promise<LeadTimeDataPoint[]> {
  return get<LeadTimeDataPoint[]>(`/reports/lead-time?${leadTimeParamsToQuery(params)}`)
}
