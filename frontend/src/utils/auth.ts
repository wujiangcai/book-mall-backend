export const getToken = (): string | null => localStorage.getItem('token')

export const getRole = (): number | null => {
  const raw = localStorage.getItem('role')
  if (raw == null) return null
  const parsed = Number(raw)
  return Number.isNaN(parsed) ? null : parsed
}

export const getUserId = (): number | null => {
  const raw = localStorage.getItem('userId')
  if (raw == null) return null
  const parsed = Number(raw)
  return Number.isNaN(parsed) ? null : parsed
}

export const getUsername = (): string | null => localStorage.getItem('username')

export const setToken = (token: string) => {
  localStorage.setItem('token', token)
}

export const setRole = (role: number) => {
  localStorage.setItem('role', String(role))
}

export const setUserId = (userId: number) => {
  localStorage.setItem('userId', String(userId))
}

export const setUsername = (username: string) => {
  localStorage.setItem('username', username)
}

export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
}
