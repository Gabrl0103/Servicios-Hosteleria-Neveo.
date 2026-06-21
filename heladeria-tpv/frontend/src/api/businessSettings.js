import client from './client'

export async function getBusinessSettings() {
  const { data } = await client.get('/business-settings')
  return data
}

export async function updateBusinessSettings(settings) {
  const { data } = await client.put('/business-settings', settings)
  return data
}
