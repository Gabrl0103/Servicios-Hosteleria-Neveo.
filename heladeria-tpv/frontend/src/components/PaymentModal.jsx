import { useState, useMemo } from 'react'
import { formatCurrency } from '../utils/format'

const BILLS = [10000, 20000, 50000, 100000]

const METHODS = [
  { value: 'EFECTIVO', label: 'Efectivo', color: '#27A567' },
  { value: 'NEQUI', label: 'Nequi', color: '#7A4FA3' },
  { value: 'RAPPI', label: 'Rappi', color: '#E0518A' },
]

// subtotal: total antes de descuento. onConfirm recibe { paymentMethod, amountReceived?, discountPercent? }
export default function PaymentModal({ subtotal, onConfirm, onCancel, loading }) {
  const [method, setMethod] = useState('EFECTIVO')
  const [received, setReceived] = useState(null)
  const [discountPercent, setDiscountPercent] = useState('')
  const [manualAmount, setManualAmount] = useState('')
  const [inputSource, setInputSource] = useState(null)

  const discountValue = Number(discountPercent) || 0
  const discountAmount = subtotal * (discountValue / 100)
  const total = Math.max(0, subtotal - discountAmount)

  const effectiveReceived = inputSource === 'manual' ? (Number(manualAmount) || null) : received

  const change = useMemo(() => {
    if (method !== 'EFECTIVO' || effectiveReceived == null) return null
    return effectiveReceived - total
  }, [method, effectiveReceived, total])

  const canConfirm = (effectiveReceived == null || effectiveReceived >= total) && discountValue >= 0 && discountValue <= 100

  function selectBill(amount) {
    setManualAmount('')
    setInputSource('bills')
    setReceived((prev) => (prev || 0) + amount)
  }

  function handleManualChange(value) {
    setManualAmount(value)
    setInputSource(value ? 'manual' : null)
    setReceived(null)
  }

  function handleConfirm() {
    const payload = { paymentMethod: method }
    if (method === 'EFECTIVO' && effectiveReceived != null) {
      payload.amountReceived = effectiveReceived
    }
    if (discountValue > 0) {
      payload.discountPercent = discountValue
    }
    onConfirm(payload)
  }

  const methodInfo = METHODS.find((m) => m.value === method)
  const changeColor = change == null ? 'var(--ink)' : change >= 0 ? 'var(--green-text)' : 'var(--red-text)'
  const changeBg = change == null ? 'var(--surface-2)' : change >= 0 ? 'var(--green-bg)' : 'var(--red-bg)'
  const changeBorder = change == null ? 'var(--border)' : change >= 0 ? 'var(--green-border)' : '#f1d4d4'

  return (
    <div
      onClick={onCancel}
      style={{
        position: 'absolute',
        inset: 0,
        background: 'rgba(42,38,34,.42)',
        backdropFilter: 'blur(2px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 40,
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{ background: '#fff', width: 500, borderRadius: 24, boxShadow: 'var(--shadow-modal)', overflow: 'hidden', maxHeight: '90vh', display: 'flex', flexDirection: 'column' }}
      >
        <div style={{ padding: '22px 26px', background: 'var(--ink)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flex: 'none' }}>
          <div>
            <div style={{ fontSize: 13, fontWeight: 800, color: '#bdb2a3', textTransform: 'uppercase', letterSpacing: '.06em' }}>
              Total a cobrar
            </div>
            <div className="mono" style={{ fontSize: 34, fontWeight: 700, marginTop: 2 }}>{formatCurrency(total)}</div>
            {discountValue > 0 && (
              <div className="mono" style={{ fontSize: 12.5, color: '#bdb2a3', marginTop: 2 }}>
                {formatCurrency(subtotal)} − {discountValue}% ({formatCurrency(discountAmount)})
              </div>
            )}
          </div>
          <button
            onClick={onCancel}
            style={{ width: 36, height: 36, borderRadius: 11, background: 'rgba(255,255,255,.14)', color: '#fff', fontSize: 18, fontWeight: 800 }}
          >
            <svg width="14" height="14"><use href="#ic-x" /></svg>
          </button>
        </div>

        <div style={{ padding: '24px 26px', overflowY: 'auto' }}>
          <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--text-soft)', textTransform: 'uppercase', letterSpacing: '.05em', marginBottom: 11 }}>
            Descuento <span style={{ textTransform: 'none', letterSpacing: 0, color: 'var(--text-faint-2)', fontWeight: 700 }}>(opcional, %)</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 22 }}>
            <input
              type="number"
              min="0"
              max="100"
              value={discountPercent}
              onChange={(e) => setDiscountPercent(e.target.value)}
              placeholder="0"
              style={{ width: 90, border: '1px solid var(--border)', borderRadius: 10, padding: '10px 12px', fontSize: 16, fontWeight: 700 }}
            />
            <span style={{ fontSize: 16, fontWeight: 800, color: 'var(--text-muted)' }}>%</span>
            {discountValue > 0 && (
              <span className="mono" style={{ fontSize: 13, color: 'var(--green-text)', fontWeight: 700 }}>
                Ahorra {formatCurrency(discountAmount)}
              </span>
            )}
          </div>

          <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--text-soft)', textTransform: 'uppercase', letterSpacing: '.05em', marginBottom: 11 }}>
            Método de pago
          </div>
          <div style={{ display: 'flex', gap: 10, marginBottom: 22 }}>
            {METHODS.map((m) => {
              const active = method === m.value
              return (
                <button
                  key={m.value}
                  onClick={() => setMethod(m.value)}
                  style={{
                    flex: 1,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 8,
                    padding: '13px 0',
                    borderRadius: 13,
                    fontSize: 14,
                    fontWeight: 800,
                    border: active ? `2px solid ${m.color}` : '1px solid var(--border)',
                    background: active ? m.color + '14' : '#fff',
                    color: 'var(--ink)',
                  }}
                >
                  <span style={{ width: 11, height: 11, borderRadius: '50%', background: m.color }} />
                  {m.label}
                </button>
              )
            })}
          </div>

          {method === 'EFECTIVO' ? (
            <div>
              <div style={{ fontSize: 13, fontWeight: 800, color: 'var(--text-soft)', textTransform: 'uppercase', letterSpacing: '.05em', marginBottom: 11 }}>
                Efectivo recibido{' '}
                <span style={{ textTransform: 'none', letterSpacing: 0, color: 'var(--text-faint-2)', fontWeight: 700 }}>(opcional)</span>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 9, marginBottom: 14 }}>
                {BILLS.map((bill) => (
                  <button
                    key={bill}
                    onClick={() => selectBill(bill)}
                    style={{ padding: '12px 4px', borderRadius: 11, border: '1px solid var(--border)', background: '#fff', fontSize: 13, fontWeight: 800, color: 'var(--ink)' }}
                  >
                    {formatCurrency(bill)}
                  </button>
                ))}
              </div>
              <div style={{ display: 'flex', gap: 12, marginBottom: 14 }}>
                <div style={{ flex: 1, background: 'var(--surface-2)', border: '1px solid var(--border)', borderRadius: 13, padding: '13px 16px', position: 'relative' }}>
                  <div style={{ fontSize: 12, color: 'var(--text-soft)', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '.04em' }}>
                    Recibido
                  </div>
                  <div className="mono" style={{ fontSize: 20, fontWeight: 700, color: 'var(--ink)', marginTop: 3 }}>
                    {effectiveReceived != null ? formatCurrency(effectiveReceived) : '—'}
                  </div>
                  {effectiveReceived != null && (
                    <button
                      onClick={() => { setReceived(null); setManualAmount(''); setInputSource(null) }}
                      aria-label="Limpiar"
                      style={{ position: 'absolute', top: 8, right: 8, background: 'transparent', color: 'var(--text-faint)', padding: 2 }}
                    >
                      <svg width="12" height="12"><use href="#ic-x" /></svg>
                    </button>
                  )}
                </div>
                <div style={{ flex: 1, background: changeBg, border: `1px solid ${changeBorder}`, borderRadius: 13, padding: '13px 16px' }}>
                  <div style={{ fontSize: 12, color: 'var(--text-soft)', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '.04em' }}>
                    Cambio
                  </div>
                  <div className="mono" style={{ fontSize: 20, fontWeight: 700, color: changeColor, marginTop: 3 }}>
                    {change == null ? '—' : formatCurrency(change)}
                  </div>
                </div>
              </div>

              <div style={{ marginBottom: 22 }}>
                <div style={{ fontSize: 11.5, fontWeight: 700, color: 'var(--text-faint)', marginBottom: 6 }}>
                  O escribe el monto manualmente
                </div>
                <input
                  type="number"
                  min="0"
                  value={manualAmount}
                  onChange={(e) => handleManualChange(e.target.value)}
                  placeholder="$0"
                  style={{ width: '100%', border: '1px solid var(--border)', borderRadius: 10, padding: '10px 12px', fontSize: 16, fontWeight: 700 }}
                />
              </div>
              {change != null && change < 0 && (
                <p style={{ marginTop: -14, marginBottom: 18, fontSize: 13, color: 'var(--red-text)', fontWeight: 700 }}>
                  El monto recibido es menor al total
                </p>
              )}
            </div>
          ) : (
            <div
              style={{
                background: 'var(--surface-2)',
                border: '1px dashed var(--border-strong)',
                borderRadius: 14,
                padding: 24,
                textAlign: 'center',
                marginBottom: 22,
              }}
            >
              <div style={{ fontSize: 15, fontWeight: 800, color: 'var(--ink)' }}>Cobro por {methodInfo.label}</div>
              <div className="mono" style={{ fontSize: 13, color: 'var(--text-soft)', fontWeight: 700, marginTop: 6 }}>
                Confirma que el pago de {formatCurrency(total)} llegó antes de finalizar.
              </div>
            </div>
          )}

          <button
            disabled={!canConfirm || loading}
            onClick={handleConfirm}
            className="btn-accent"
            style={{ width: '100%', fontSize: 18, padding: 17 }}
          >
            {loading ? 'Procesando...' : 'Confirmar venta'}
          </button>
        </div>
      </div>
    </div>
  )
}
