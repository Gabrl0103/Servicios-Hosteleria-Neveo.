import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSession } from '../context/SessionContext'
import { openCashRegister, closeCashRegister } from '../api/cashRegisters'
import { getCashiers } from '../api/cashiers'
import { getShiftReport, getExpectedCash } from '../api/reports'
import { getExpenses, createExpense, deleteExpense } from '../api/expenses'
import { formatCurrency } from '../utils/format'

const METHODS = [
  { value: 'EFECTIVO', label: 'Efectivo', color: '#27A567' },
  { value: 'NEQUI', label: 'Nequi', color: '#7A4FA3' },
  { value: 'RAPPI', label: 'Rappi', color: '#E0518A' },
]

export default function CashRegisterPage() {
  const { user, cashRegister, refreshCashRegister } = useSession()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [report, setReport] = useState(null)
  const navigate = useNavigate()

  const [showOpenPanel, setShowOpenPanel] = useState(false)
  const [cashiers, setCashiers] = useState(null)
  const [selectedCashier, setSelectedCashier] = useState(null)
  const [confirmed, setConfirmed] = useState(false)
  const [openingAmount, setOpeningAmount] = useState('')
  const [expectedCashData, setExpectedCashData] = useState(null)
  const [expenses, setExpenses] = useState([])
  const [expDesc, setExpDesc] = useState('')
  const [expAmount, setExpAmount] = useState('')
  const [expLoading, setExpLoading] = useState(false)

  function refreshExpenseData() {
    getExpenses(cashRegister.id).then(setExpenses).catch(() => setExpenses([]))
    getExpectedCash(cashRegister.id).then(setExpectedCashData).catch(() => setExpectedCashData(null))
  }

  useEffect(() => {
    if (cashRegister) {
      getShiftReport(cashRegister.id).then(setReport).catch(() => setReport(null))
      getExpectedCash(cashRegister.id).then(setExpectedCashData).catch(() => setExpectedCashData(null))
      getExpenses(cashRegister.id).then(setExpenses).catch(() => setExpenses([]))
    }
  }, [cashRegister])

  function handleOpenClick() {
    setShowOpenPanel(true)
    setSelectedCashier(null)
    setConfirmed(false)
    getCashiers().then(setCashiers).catch(() => setCashiers([]))
  }

  async function handleConfirmOpen() {
    setLoading(true)
    setError('')
    try {
      const amount = openingAmount ? Number(openingAmount) : null
      await openCashRegister(user.id, selectedCashier.name, amount)
      await refreshCashRegister()
      navigate('/mesas')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleClose() {
    setLoading(true)
    setError('')
    try {
      await closeCashRegister(cashRegister.id)
      await refreshCashRegister()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const canOpen = selectedCashier && confirmed

  return (
    <div style={{ position: 'absolute', inset: 0, overflowY: 'auto', padding: 32 }}>
      <div style={{ maxWidth: 880, margin: '0 auto' }}>
        <h1 style={{ fontSize: 25, fontWeight: 900, margin: '0 0 4px' }}>Turno de hoy</h1>
        <p style={{ fontSize: 14, color: 'var(--text-soft)', fontWeight: 700, margin: '0 0 24px' }}>
          Controla la apertura y el cierre de la caja del día.
        </p>

        {error && <p style={{ color: 'var(--red-text)', fontSize: 14 }}>{error}</p>}

        {!cashRegister ? (
          !showOpenPanel ? (
            <div className="card" style={{ padding: 26, textAlign: 'center' }}>
              <p style={{ fontSize: 15, color: 'var(--text-muted)', marginBottom: 18 }}>
                No hay un turno abierto en este momento.
              </p>
              <button onClick={handleOpenClick} disabled={loading} className="btn-ink" style={{ padding: '16px 32px', fontSize: 16 }}>
                Abrir turno
              </button>
            </div>
          ) : (
            <div className="card" style={{ padding: 26 }}>
              <div style={{ fontSize: 17, fontWeight: 900, color: 'var(--ink)', marginBottom: 4 }}>Abrir turno</div>
              <p style={{ fontSize: 13, color: 'var(--text-soft)', fontWeight: 700, margin: '0 0 18px' }}>
                Selecciona quién atenderá la caja y confirma la apertura.
              </p>

              {cashiers === null ? (
                <p style={{ color: 'var(--text-soft)', fontSize: 14 }}>Cargando cajeros...</p>
              ) : cashiers.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '20px 0' }}>
                  <p style={{ fontSize: 15, color: 'var(--text-muted)', marginBottom: 14 }}>
                    No hay cajeros configurados.
                  </p>
                  <button
                    onClick={() => navigate('/configuracion')}
                    className="btn-ink"
                    style={{ padding: '12px 22px', fontSize: 14 }}
                  >
                    Ir a Configuración
                  </button>
                </div>
              ) : (
                <>
                  <div style={{ fontSize: 12, fontWeight: 800, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '.04em', marginBottom: 10 }}>
                    Cajero
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 22 }}>
                    {cashiers.map((c) => (
                      <button
                        key={c.id}
                        onClick={() => setSelectedCashier(c)}
                        style={{
                          padding: '12px 20px',
                          borderRadius: 11,
                          fontSize: 14,
                          fontWeight: 800,
                          border: selectedCashier?.id === c.id ? 'none' : '1px solid var(--border)',
                          background: selectedCashier?.id === c.id ? 'var(--ink)' : '#fff',
                          color: selectedCashier?.id === c.id ? '#fff' : 'var(--text-muted)',
                          cursor: 'pointer',
                        }}
                      >
                        {c.name}
                      </button>
                    ))}
                  </div>

                  <div style={{ marginBottom: 22 }}>
                    <div style={{ fontSize: 12, fontWeight: 800, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '.04em', marginBottom: 6 }}>
                      Valor inicial de caja <span style={{ textTransform: 'none', letterSpacing: 0, fontWeight: 700 }}>(opcional)</span>
                    </div>
                    <input
                      type="number"
                      min="0"
                      value={openingAmount}
                      onChange={(e) => setOpeningAmount(e.target.value)}
                      placeholder="$0"
                      style={{ width: 200, border: '1px solid var(--border)', borderRadius: 10, padding: '10px 12px', fontSize: 15, fontWeight: 700 }}
                    />
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 22 }}>
                    <button
                      onClick={() => setConfirmed(!confirmed)}
                      style={{
                        width: 48,
                        height: 28,
                        borderRadius: 14,
                        border: 'none',
                        background: confirmed ? 'var(--ink)' : 'var(--border)',
                        position: 'relative',
                        cursor: 'pointer',
                        transition: 'background .2s',
                        flex: 'none',
                      }}
                    >
                      <span
                        style={{
                          position: 'absolute',
                          top: 3,
                          left: confirmed ? 23 : 3,
                          width: 22,
                          height: 22,
                          borderRadius: '50%',
                          background: '#fff',
                          transition: 'left .2s',
                          boxShadow: '0 1px 3px rgba(0,0,0,.18)',
                        }}
                      />
                    </button>
                    <span style={{ fontSize: 14, fontWeight: 700, color: confirmed ? 'var(--ink)' : 'var(--text-muted)' }}>
                      Confirmo la apertura del turno
                    </span>
                  </div>

                  <div style={{ display: 'flex', gap: 10 }}>
                    <button
                      onClick={handleConfirmOpen}
                      disabled={!canOpen || loading}
                      className="btn-ink"
                      style={{
                        padding: '14px 28px',
                        fontSize: 15,
                        opacity: canOpen ? 1 : 0.4,
                      }}
                    >
                      {loading ? 'Abriendo...' : 'Abrir turno'}
                    </button>
                    <button
                      onClick={() => setShowOpenPanel(false)}
                      style={{
                        padding: '14px 22px',
                        fontSize: 15,
                        fontWeight: 800,
                        border: '1px solid var(--border)',
                        background: '#fff',
                        color: 'var(--text-muted)',
                        borderRadius: 14,
                        cursor: 'pointer',
                      }}
                    >
                      Cancelar
                    </button>
                  </div>
                </>
              )}
            </div>
          )
        ) : (
          <>
            <div className="card" style={{ padding: 26, marginBottom: 20, display: 'flex', alignItems: 'center', gap: 22 }}>
              <div style={{ width: 64, height: 64, borderRadius: 16, background: 'var(--green-bg)', display: 'grid', placeItems: 'center', flex: 'none' }}>
                <span style={{ width: 16, height: 16, borderRadius: '50%', background: 'var(--green-dot)', boxShadow: '0 0 0 5px rgba(39,165,103,.18)' }} />
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--green-text)', letterSpacing: '.04em', textTransform: 'uppercase' }}>
                  Turno abierto
                </div>
                <div style={{ fontSize: 23, fontWeight: 900, color: 'var(--ink)', marginTop: 2 }}>Caja 01 · en curso</div>
                <div className="mono" style={{ fontSize: 13, color: 'var(--text-soft-2)', fontWeight: 700, marginTop: 4 }}>
                  Inicio {new Date(cashRegister.openedAt).toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit' })}
                  {' · '}
                  {new Date(cashRegister.openedAt).toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
                  {report ? ` · ${report.totalOrders} ventas` : ''}
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: 12, fontWeight: 800, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '.04em' }}>
                  Acumulado
                </div>
                <div className="mono" style={{ fontSize: 30, fontWeight: 700, color: 'var(--accent)' }}>
                  {report ? formatCurrency(report.totalAmount) : '—'}
                </div>
              </div>
            </div>

            <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '.06em', margin: '0 0 12px' }}>
              Resumen en vivo por método
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: 14, marginBottom: 26 }}>
              {METHODS.map((m) => (
                <div key={m.value} className="card" style={{ padding: 18 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                    <span style={{ width: 10, height: 10, borderRadius: '50%', background: m.color }} />
                    <span style={{ fontSize: 13.5, fontWeight: 800, color: '#5c554c' }}>{m.label}</span>
                  </div>
                  <div className="mono" style={{ fontSize: 22, fontWeight: 700, color: 'var(--ink)' }}>
                    {formatCurrency(report?.breakdown[m.value]?.total || 0)}
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-soft)', fontWeight: 700, marginTop: 3 }}>
                    {report?.breakdown[m.value]?.orderCount || 0} ventas
                  </div>
                </div>
              ))}
              <div className="card" style={{ padding: 18, background: 'var(--ink)', border: 'none' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                  <span style={{ fontSize: 13.5, fontWeight: 800, color: '#bdb2a3' }}>Total</span>
                </div>
                <div className="mono" style={{ fontSize: 22, fontWeight: 700, color: '#fff' }}>
                  {formatCurrency(report?.totalAmount || 0)}
                </div>
                <div style={{ fontSize: 12, color: '#bdb2a3', fontWeight: 700, marginTop: 3 }}>
                  {report?.totalOrders || 0} ventas
                </div>
              </div>
            </div>

            {expectedCashData && (
              <div className="card" style={{ padding: 18, marginBottom: 26, display: 'inline-flex', alignItems: 'center', gap: 18, border: '2px solid var(--green-border)', background: 'var(--green-bg)' }}>
                <div>
                  <div style={{ fontSize: 12, fontWeight: 800, color: 'var(--green-text)', textTransform: 'uppercase', letterSpacing: '.04em' }}>
                    Esperado en caja
                  </div>
                  <div className="mono" style={{ fontSize: 26, fontWeight: 700, color: 'var(--green-text)', marginTop: 2 }}>
                    {formatCurrency(expectedCashData.expectedCash)}
                  </div>
                  <div style={{ fontSize: 11.5, color: 'var(--text-soft)', fontWeight: 700, marginTop: 4 }}>
                    Caja inicial: {formatCurrency(expectedCashData.openingAmount)} + Efectivo: {formatCurrency(expectedCashData.cashSales)}
                    {expectedCashData.totalExpenses > 0 && ` − Gastos: ${formatCurrency(expectedCashData.totalExpenses)}`}
                  </div>
                </div>
              </div>
            )}

            <div className="card" style={{ padding: 22, marginBottom: 26 }}>
              <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--text-faint)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 14 }}>
                Gastos del turno
              </div>
              <div style={{ display: 'flex', gap: 10, marginBottom: 16 }}>
                <input
                  type="text"
                  placeholder="Descripción"
                  value={expDesc}
                  onChange={(e) => setExpDesc(e.target.value)}
                  style={{ flex: 1, border: '1px solid var(--border)', borderRadius: 10, padding: '10px 12px', fontSize: 14, fontWeight: 700 }}
                />
                <input
                  type="number"
                  min="0"
                  placeholder="$0"
                  value={expAmount}
                  onChange={(e) => setExpAmount(e.target.value)}
                  style={{ width: 120, border: '1px solid var(--border)', borderRadius: 10, padding: '10px 12px', fontSize: 14, fontWeight: 700 }}
                />
                <button
                  disabled={!expDesc.trim() || !expAmount || Number(expAmount) <= 0 || expLoading}
                  className="btn-ink"
                  style={{ padding: '10px 18px', fontSize: 13, opacity: expDesc.trim() && expAmount && Number(expAmount) > 0 ? 1 : 0.4 }}
                  onClick={async () => {
                    setExpLoading(true)
                    try {
                      await createExpense(cashRegister.id, expDesc.trim(), Number(expAmount))
                      setExpDesc('')
                      setExpAmount('')
                      refreshExpenseData()
                    } catch (err) {
                      setError(err.message)
                    } finally {
                      setExpLoading(false)
                    }
                  }}
                >
                  Agregar gasto
                </button>
              </div>

              {expenses.length > 0 && (
                <>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                    {expenses.map((exp) => (
                      <div key={exp.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 10px', background: '#faf8f5', borderRadius: 8 }}>
                        <span style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--ink)' }}>{exp.description}</span>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                          <span className="mono" style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--accent)' }}>{formatCurrency(exp.amount)}</span>
                          <button
                            onClick={async () => {
                              try {
                                await deleteExpense(exp.id)
                                refreshExpenseData()
                              } catch (err) {
                                setError(err.message)
                              }
                            }}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 4, fontSize: 15, color: 'var(--text-soft)' }}
                            title="Eliminar gasto"
                          >
                            🗑
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12, paddingTop: 10, borderTop: '1px solid var(--border)' }}>
                    <span style={{ fontSize: 14, fontWeight: 800, color: 'var(--ink)' }}>Total gastos</span>
                    <span className="mono" style={{ fontSize: 14, fontWeight: 800, color: 'var(--accent)' }}>
                      {formatCurrency(expenses.reduce((sum, e) => sum + Number(e.amount), 0))}
                    </span>
                  </div>
                </>
              )}
            </div>

            <div style={{ display: 'flex', gap: 14 }}>
              <button onClick={() => navigate('/mesas')} className="btn-ink" style={{ flex: 1, fontSize: 17, padding: 18 }}>
                Ir a mesas →
              </button>
              <button
                onClick={handleClose}
                disabled={loading}
                style={{
                  flex: 'none',
                  padding: '18px 26px',
                  border: '2px solid var(--accent)',
                  background: '#fff',
                  color: 'var(--accent)',
                  fontSize: 17,
                  fontWeight: 800,
                  borderRadius: 14,
                }}
              >
                {loading ? 'Cerrando...' : 'Cerrar turno'}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
