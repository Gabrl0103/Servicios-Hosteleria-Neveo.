package com.heladeria.tpv.dto;

import java.math.BigDecimal;

public class DashboardKpisResponse {

    private BigDecimal currentShiftTotal;
    private BigDecimal last7DaysTotal;
    private BigDecimal last30DaysTotal;
    private BigDecimal yearTotal;

    public DashboardKpisResponse(BigDecimal currentShiftTotal, BigDecimal last7DaysTotal,
                                  BigDecimal last30DaysTotal, BigDecimal yearTotal) {
        this.currentShiftTotal = currentShiftTotal;
        this.last7DaysTotal = last7DaysTotal;
        this.last30DaysTotal = last30DaysTotal;
        this.yearTotal = yearTotal;
    }

    public BigDecimal getCurrentShiftTotal() {
        return currentShiftTotal;
    }

    public BigDecimal getLast7DaysTotal() {
        return last7DaysTotal;
    }

    public BigDecimal getLast30DaysTotal() {
        return last30DaysTotal;
    }

    public BigDecimal getYearTotal() {
        return yearTotal;
    }
}
