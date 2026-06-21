package com.heladeria.tpv.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySalesPoint {

    private LocalDate date;
    private BigDecimal total;
    private long orderCount;

    public DailySalesPoint(LocalDate date, BigDecimal total, long orderCount) {
        this.date = date;
        this.total = total;
        this.orderCount = orderCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
