import client from './client'

export async function getExpenses(cashRegisterId) {
  const { data } = await client.get('/expenses', { params: { cashRegisterId } })
  return data
}

export async function createExpense(cashRegisterId, description, amount) {
  const { data } = await client.post('/expenses', { cashRegisterId, description, amount })
  return data
}

export async function deleteExpense(id) {
  await client.delete(`/expenses/${id}`)
}
