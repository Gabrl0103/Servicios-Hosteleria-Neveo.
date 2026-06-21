import { useState } from 'react'
import { formatCurrency } from '../utils/format'
import { categoryInfo } from '../utils/categoryColors'

// Muestra el resumen del pedido con un campo de observacion editable
// por cada producto, antes de pasar al modal de pago.
export default function ConfirmationModal({ cart, onUpdateNote, onConfirm, onCancel }) {
  const total = cart.reduce((sum, i) => sum + i.product.price * i.quantity, 0)

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
        style={{ background: '#fff', width: 460, borderRadius: 22, boxShadow: 'var(--shadow-modal)', overflow: 'hidden', maxHeight: '88vh', display: 'flex', flexDirection: 'column' }}
      >
        <div style={{ padding: '20px 24px', borderBottom: '1px solid var(--border-soft)' }}>
          <div style={{ fontSize: 18, fontWeight: 900, color: 'var(--ink)' }}>Confirmar pedido</div>
          <div style={{ fontSize: 13, color: 'var(--text-soft)', fontWeight: 700, marginTop: 2 }}>
            Revisa los productos y agrega observaciones si hace falta.
          </div>
        </div>

        <div style={{ padding: '14px 24px', overflowY: 'auto', flex: 1 }}>
          {cart.map((item) => {
            const info = categoryInfo(item.product.category)
            return (
              <div key={item.product.id} style={{ padding: '12px 0', borderBottom: '1px solid var(--border-soft-2)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                  <span style={{ width: 9, height: 9, borderRadius: '50%', background: info.color, flex: 'none' }} />
                  <span style={{ flex: 1, fontWeight: 800, color: 'var(--ink)', fontSize: 14.5 }}>
                    {item.product.name} <span className="mono" style={{ color: 'var(--text-soft)', fontWeight: 700 }}>x{item.quantity}</span>
                  </span>
                  <span className="mono" style={{ fontSize: 14, fontWeight: 700, color: 'var(--ink)' }}>
                    {formatCurrency(item.product.price * item.quantity)}
                  </span>
                </div>
                <input
                  value={item.note || ''}
                  onChange={(e) => onUpdateNote(item.product.id, e.target.value)}
                  placeholder="Observación (ej: sin azúcar)"
                  style={{ width: '100%', border: '1px solid var(--border)', borderRadius: 9, padding: '8px 11px', fontSize: 13 }}
                />
              </div>
            )
          })}
        </div>

        <div style={{ padding: '16px 24px', borderTop: '1px solid var(--border-soft)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 14 }}>
            <span style={{ fontSize: 15, fontWeight: 800 }}>Total</span>
            <span className="mono" style={{ fontSize: 19, fontWeight: 700, color: 'var(--accent)' }}>{formatCurrency(total)}</span>
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button onClick={onCancel} className="btn-outline" style={{ flex: 1, padding: 13 }}>
              Cancelar
            </button>
            <button onClick={onConfirm} className="btn-ink" style={{ flex: 1, padding: 13 }}>
              Continuar a cobrar
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
