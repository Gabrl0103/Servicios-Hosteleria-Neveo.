package com.heladeria.tpv.dto;

import java.math.BigDecimal;

public class BarChartPoint {

    private String label;
    private BigDecimal total;

    public BarChartPoint(String label, BigDecimal total) {
        this.label = label;
        this.total = total;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
