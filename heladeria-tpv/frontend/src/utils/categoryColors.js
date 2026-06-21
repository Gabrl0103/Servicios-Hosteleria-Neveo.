// Mapa de categorias reales del negocio a icono + color, segun el
// design handoff de Claude Design. La clave es el nombre de categoria
// tal cual se guarda en el backend (case-insensitive, sin tildes).
const CATEGORY_MAP = {
  helados: { icon: 'helados', color: '#E0518A', short: 'Helados' },
  acai: { icon: 'acai', color: '#7A4FA3', short: 'Acaí' },
  cafe: { icon: 'cafe', color: '#6F4E37', short: 'Café' },
  cookies: { icon: 'cookies', color: '#C99A52', short: 'Cookies' },
  'miti-miti': { icon: 'mitimiti', color: '#2BA89B', short: 'Miti-miti' },
  mitimiti: { icon: 'mitimiti', color: '#2BA89B', short: 'Miti-miti' },
  'adicion de fruta': { icon: 'fruta', color: '#C0392B', short: 'Ad. fruta' },
  fruta: { icon: 'fruta', color: '#C0392B', short: 'Ad. fruta' },
  'adicion de salsas': { icon: 'salsas', color: '#B5651D', short: 'Ad. salsas' },
  salsas: { icon: 'salsas', color: '#B5651D', short: 'Ad. salsas' },
  toppings: { icon: 'toppings', color: '#E08A1E', short: 'Toppings' },
}

const FALLBACK = { icon: 'helados', color: '#8a8175', short: '' }

function normalize(str) {
  return (str || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
}

export function categoryInfo(category) {
  const key = normalize(category)
  return CATEGORY_MAP[key] || { ...FALLBACK, short: category || 'Otros' }
}

export function categoryTint(hex) {
  // Aplica un alpha bajo (~12%) al color hex, igual al "tint" del diseño.
  return hex + '1f'
}
