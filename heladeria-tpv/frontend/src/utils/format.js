export function formatCurrency(value) {
  const number = Number(value || 0)
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(number)
}
