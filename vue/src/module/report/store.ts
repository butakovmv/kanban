import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as reportApi from './api'

export const useReportStore = defineStore('report', () => {
  const cfdData = ref<reportApi.CfdDataPoint[]>([])
  const leadTimeData = ref<reportApi.LeadTimeDataPoint[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadCfd(params: reportApi.CfdParams): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      cfdData.value = await reportApi.getCfd(params)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load CFD data'
      cfdData.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  async function loadLeadTime(params: reportApi.LeadTimeParams): Promise<boolean> {
    loading.value = true
    error.value = null
    try {
      leadTimeData.value = await reportApi.getLeadTime(params)
      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : 'Failed to load lead time data'
      leadTimeData.value = []
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    cfdData,
    leadTimeData,
    loading,
    error,
    loadCfd,
    loadLeadTime,
  }
})
