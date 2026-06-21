import { useEffect, useState } from 'react'
import { getAllCashRegisters } from '../api/cashRegisters'
import { getShiftReport } from '../api/reports'
import { formatCurrency } from '../utils/format'

function formatDateTime(iso) {
  if (!iso) return '—'
  return (
    new Date(iso).toLocaleDateString('es-CO', { day: 'numeric', month: 'short' }) +
    ' · ' +
    new Date(iso).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })
  )
}

export default function CashBoxHistoryPage() {
  const [registers, setRegisters] = useState([])
  const [totals, setTotals] = useState({})
  const [error, setError] = useState('')

  useEffect(() => {
    getAllCashRegisters()
      .then(async (list) => {
        setRegisters(list)
        const entries = await Promise.all(
          list.map(async (r) => {
            try {
              const report = await getShiftReport(r.id)
              return [r.id, report]
            } catch {
              return [r.id, null]
            }
          })
        )
        setTotals(Object.fromEntries(entries))
      })
      .catch((err) => setError(err.message))
  }, [])

  function openReceipt(cashRegisterId) {
    // Pestana/ventana aparte, igual al patron de Loggro (cashBox-print).
    // En la app empaquetada con Electron, esto se intercepta para abrir
    // una BrowserWindow nueva en vez de una pestana del navegador.
    window.open(`/recibo/${cashRegisterId}`, '_blank', 'width=420,height=720')
  }

  return (
    <div style={{ position: 'absolute', inset: 0, overflowY: 'auto', padding: 32 }}>
      <div style={{ maxWidth: 1100, margin: '0 auto' }}>
        <h1 style={{ fontSize: 25, fontWeight: 900, margin: '0 0 4px' }}>Cuadre de caja</h1>
        <p style={{ fontSize: 14, color: 'var(--text-soft)', fontWeight: 700, margin: '0 0 24px' }}>
          Historial de turnos. Abre el comprobante de cierre de cualquiera en una ventana aparte.
        </p>

        {error && <p style={{ color: 'var(--red-text)' }}>{error}</p>}

        <div className="card" style={{ overflow: 'hidden', padding: 0 }}>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: '1.3fr 1.3fr 1fr 1fr .8fr .9fr .9fr',
              padding: '14px 22px',
              background: 'var(--surface-2)',
              borderBottom: '1px solid var(--border)',
              fontSize: 12,
              fontWeight: 800,
              color: 'var(--text-faint)',
              textTransform: 'uppercase',
              letterSpacing: '.04em',
            }}
          >
            <span>Fecha inicio</span>
            <span>Fecha fin</span>
            <span>Nombre de caja</span>
            <span>Responsable</span>
            <span>Ventas</span>
            <span>Total</span>
            <span></span>
          </div>

          {registers.length === 0 && !error && (
            <p style={{ padding: 22, color: 'var(--text-soft)', fontSize: 14 }}>Aún no hay turnos registrados.</p>
          )}

          {registers.map((register) => {
            const report = totals[register.id]
            const isOpen = !register.closedAt
            return (
              <div
                key={register.id}
                style={{
                  display: 'grid',
                  gridTemplateColumns: '1.3fr 1.3fr 1fr 1fr .8fr .9fr .9fr',
                  alignItems: 'center',
                  padding: '16px 22px',
                  borderBottom: '1px solid var(--border-soft-2)',
                }}
              >
                <span className="mono" style={{ color: 'var(--ink)', fontWeight: 700, fontSize: 13 }}>
                  {formatDateTime(register.openedAt)}
                </span>
                <span className="mono" style={{ color: isOpen ? 'var(--text-faint)' : 'var(--ink)', fontWeight: 700, fontSize: 13 }}>
                  {isOpen ? (
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'var(--green-dot)' }} />
                      En curso
                    </span>
                  ) : (
                    formatDateTime(register.closedAt)
                  )}
                </span>
                <span style={{ fontWeight: 700, color: 'var(--ink)', fontSize: 14 }}>Caja principal</span>
                <span style={{ color: 'var(--text-muted)', fontWeight: 700, fontSize: 14 }}>{register.cashierName || '—'}</span>
                <span className="mono" style={{ color: 'var(--ink)', fontWeight: 700 }}>{report?.totalOrders ?? '—'}</span>
                <span className="mono" style={{ color: 'var(--ink)', fontWeight: 700, fontSize: 14 }}>
                  {report ? formatCurrency(report.totalAmount) : '—'}
                </span>
                <span style={{ textAlign: 'right' }}>
                  <button
                    onClick={() => openReceipt(register.id)}
                    style={{
                      background: '#fff',
                      border: '1px solid var(--border-strong)',
                      color: 'var(--ink)',
                      fontSize: 12,
                      fontWeight: 800,
                      padding: '8px 12px',
                      borderRadius: 9,
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 6,
                    }}
                  >
                    <svg width="14" height="14"><use href="#ic-print" /></svg>
                    Comprobante
                  </button>
                </span>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}
