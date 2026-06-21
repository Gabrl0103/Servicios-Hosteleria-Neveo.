import { useState } from 'react'
import { formatCurrency } from '../utils/format'
import { categoryInfo } from '../utils/categoryColors'

const SHORTCUTS = [2, 5, 10]

export default function QuantityModal({ product, onConfirm, onCancel }) {
  const [quantity, setQuantity] = useState(1)
  const info = categoryInfo(product.category)

  function changeBy(delta) {
    setQuantity((q) => Math.max(1, q + delta))
  }

  function handleInputChange(e) {
    const value = Number(e.target.value)
    if (!Number.isNaN(value) && value >= 1) {
      setQuantity(Math.floor(value))
    } else if (e.target.value === '') {
      setQuantity('')
    }
  }

  const safeQuantity = quantity === '' ? 0 : quantity
  const subtotal = product.price * safeQuantity

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
        style={{ background: '#fff', width: 420, borderRadius: 22, boxShadow: 'var(--shadow-modal)', overflow: 'hidden' }}
      >
        <div style={{ padding: '22px 24px', borderBottom: '1px solid var(--border-soft)', display: 'flex', alignItems: 'center', gap: 13 }}>
          <span
            style={{
              width: 42,
              height: 42,
              borderRadius: 12,
              background: info.color + '1f',
              color: info.color,
              display: 'grid',
              placeItems: 'center',
              flex: 'none',
            }}
          >
            <svg width="22" height="22"><use href={`#cat-${info.icon}`} /></svg>
          </span>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 18, fontWeight: 900, color: 'var(--ink)' }}>{product.name}</div>
            <div className="mono" style={{ fontSize: 13, color: 'var(--text-soft)', fontWeight: 700 }}>
              {formatCurrency(product.price)} c/u · {info.short}
            </div>
          </div>
          <button
            onClick={onCancel}
            style={{ width: 34, height: 34, borderRadius: 10, background: 'var(--tile-bg)', color: 'var(--text-soft-2)', fontSize: 16, fontWeight: 800 }}
          >
            <svg width="14" height="14"><use href="#ic-x" /></svg>
          </button>
        </div>

        <div style={{ padding: '26px 24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 18, marginBottom: 20 }}>
            <button
              onClick={() => changeBy(-1)}
              style={{ width: 58, height: 58, borderRadius: 16, background: 'var(--tile-bg)', color: 'var(--ink)', fontSize: 30, fontWeight: 800 }}
            >
              −
            </button>
            <div style={{ width: 120, textAlign: 'center' }}>
              <input
                type="number"
                value={quantity}
                onChange={handleInputChange}
                className="mono"
                style={{
                  width: '100%',
                  border: '2px solid var(--border)',
                  borderRadius: 14,
                  textAlign: 'center',
                  fontSize: 38,
                  fontWeight: 700,
                  color: 'var(--ink)',
                  padding: '6px 0',
                }}
              />
            </div>
            <button
              onClick={() => changeBy(1)}
              style={{ width: 58, height: 58, borderRadius: 16, background: 'var(--ink)', color: '#fff', fontSize: 30, fontWeight: 800 }}
            >
              +
            </button>
          </div>

          <div style={{ display: 'flex', gap: 9, justifyContent: 'center', marginBottom: 24 }}>
            <span style={{ fontSize: 13, color: 'var(--text-soft)', fontWeight: 800, alignSelf: 'center', marginRight: 2 }}>Rápido</span>
            {SHORTCUTS.map((n) => (
              <button
                key={n}
                onClick={() => setQuantity(n)}
                style={{ padding: '9px 18px', borderRadius: 11, border: '1px solid var(--border)', background: '#fff', fontSize: 15, fontWeight: 800, color: 'var(--ink)' }}
              >
                {n}
              </button>
            ))}
          </div>

          <button
            disabled={safeQuantity < 1}
            onClick={() => onConfirm(safeQuantity)}
            className="btn-ink"
            style={{ width: '100%', fontSize: 17, padding: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
          >
            Agregar al pedido <span className="mono">· {formatCurrency(subtotal)}</span>
          </button>
        </div>
      </div>
    </div>
  )
}
