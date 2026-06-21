import client from './client'

export async function getReportByDateRange(from, to) {
  const { data } = await client.get('/reports/by-payment-method', {
    params: { from, to },
  })
  return data
}

export async function getReportSummary(from, to) {
  const { data } = await client.get('/reports/summary', {
    params: { from, to },
  })
  return data
}

export async function getDashboardKpis() {
  const { data } = await client.get('/reports/dashboard-kpis')
  return data
}

export async function getMonthlyBarChart() {
  const { data } = await client.get('/reports/monthly-bar-chart')
  return data
}

export async function getCurrentShiftReport() {
  const { data } = await client.get('/reports/current-shift')
  return data
}

export async function getShiftReport(cashRegisterId) {
  const { data } = await client.get(`/reports/shift/${cashRegisterId}`)
  return data
}

export async function getShiftReceipt(cashRegisterId) {
  const { data } = await client.get(`/reports/shift/${cashRegisterId}/receipt`)
  return data
}

export async function getExpectedCash(cashRegisterId) {
  const { data } = await client.get(`/reports/shift/${cashRegisterId}/expected-cash`)
  return data
}
