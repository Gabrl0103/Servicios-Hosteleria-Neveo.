import client from './client'

export async function getProducts(onlyAvailable = false) {
  const { data } = await client.get('/products', { params: { onlyAvailable } })
  return data
}

export async function createProduct(product) {
  const { data } = await client.post('/products', product)
  return data
}

export async function updateProduct(id, product) {
  const { data } = await client.put(`/products/${id}`, product)
  return data
}

export async function setProductAvailability(id, available) {
  const { data } = await client.patch(`/products/${id}/availability`, null, {
    params: { available },
  })
  return data
}

export async function deleteProduct(id) {
  await client.delete(`/products/${id}`)
}
