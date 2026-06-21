export function relativeTimeSince(isoDate) {
  const start = new Date(isoDate)
  const now = new Date()
  const diffMs = now - start
  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  if (hours <= 0) return `hace ${minutes}m`
  return `hace ${hours}h ${minutes}m`
}
