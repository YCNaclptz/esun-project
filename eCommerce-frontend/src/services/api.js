import axios from 'axios'

const { VITE_API_BASE_URL } = import.meta.env

if (!VITE_API_BASE_URL) {
  throw new Error('VITE_API_BASE_URL must be set')
}

const apiBaseUrl = VITE_API_BASE_URL.replace(/\/$/, '')

const api = axios.create({
  baseURL: apiBaseUrl,
})

export const isAxiosError = axios.isAxiosError
export default api
