import axios from 'axios'

// El backend Spring Boot corre siempre en localhost, en el mismo equipo.
// Cuando se empaqueta con Electron, este puerto es fijo y no cambia.
const client = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

// Convierte los errores del backend (formato { error, status }) en
// un mensaje simple y legible para mostrar en la interfaz.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.error || 'No se pudo conectar con el sistema'
    return Promise.reject(new Error(message))
  }
)

export default client
