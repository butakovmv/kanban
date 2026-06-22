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

interface RawCfdResponse {
  points: {
    date: string
    column_id: string
    column_name: string
    count: number
  }[]
}

interface RawLeadTimeResponse {
  points: {
    date: string
    task_id: string
    task_title: string
    lead_time_hours: number
  }[]
}

function toCfdDataPoint(raw: RawCfdResponse['points'][0]): CfdDataPoint {
  return {
    date: raw.date,
    columnId: raw.column_id,
    columnName: raw.column_name,
    count: raw.count,
  }
}

function toLeadTimeDataPoint(raw: RawLeadTimeResponse['points'][0]): LeadTimeDataPoint {
  return {
    date: raw.date,
    taskId: raw.task_id,
    taskTitle: raw.task_title,
    leadTimeHours: raw.lead_time_hours,
  }
}

export function getCfd(params: CfdParams): Promise<CfdDataPoint[]> {
  return get<RawCfdResponse>(`/reports/cfd?${cfdParamsToQuery(params)}`).then(
    (response) => response.points.map(toCfdDataPoint),
  )
}

export function getLeadTime(params: LeadTimeParams): Promise<LeadTimeDataPoint[]> {
  return get<RawLeadTimeResponse>(`/reports/lead-time?${leadTimeParamsToQuery(params)}`).then(
    (response) => response.points.map(toLeadTimeDataPoint),
  )
}
