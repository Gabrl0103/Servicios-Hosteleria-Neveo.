package com.heladeria.tpv.dto;

import com.heladeria.tpv.model.PaymentMethod;
import com.heladeria.tpv.repository.OrderRepository.PaymentMethodTotalsProjection;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PaymentMethodReportResponse {

    private Map<PaymentMethod, Breakdown> breakdown = new EnumMap<>(PaymentMethod.class);
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private long totalOrders = 0;

    public PaymentMethodReportResponse(List<PaymentMethodTotalsProjection> rows) {
        // Se inicializan los tres metodos en cero para que el frontend
        // siempre reciba las tres claves, aunque no haya ventas en alguno.
        for (PaymentMethod method : PaymentMethod.values()) {
            breakdown.put(method, new Breakdown(BigDecimal.ZERO, 0));
        }

        for (PaymentMethodTotalsProjection row : rows) {
            breakdown.put(row.getPaymentMethod(), new Breakdown(row.getTotal(), row.getOrderCount()));
            totalAmount = totalAmount.add(row.getTotal());
            totalOrders += row.getOrderCount();
        }
    }

    public Map<PaymentMethod, Breakdown> getBreakdown() {
        return breakdown;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public static class Breakdown {
        private BigDecimal total;
        private long orderCount;

        public Breakdown(BigDecimal total, long orderCount) {
            this.total = total;
            this.orderCount = orderCount;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public long getOrderCount() {
            return orderCount;
        }
    }
}
