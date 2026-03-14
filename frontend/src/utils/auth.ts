export const getToken = (): string | null => localStorage.getItem('token')

export const getRole = (): number | null => {
  const raw = localStorage.getItem('role')
  if (raw == null) return null
  const parsed = Number(raw)
  return Number.isNaN(parsed) ? null : parsed
}

export const setToken = (token: string) => {
  localStorage.setItem('token', token)
}

export const setRole = (role: number) => {
  localStorage.setItem('role', String(role))
}

export const clearAuth = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
}
