package com.heladeria.tpv.dto;

import java.math.BigDecimal;

public class ExpectedCashResponse {

    private BigDecimal openingAmount;
    private BigDecimal cashSales;
    private BigDecimal totalExpenses;
    private BigDecimal expectedCash;

    public ExpectedCashResponse(BigDecimal openingAmount, BigDecimal cashSales, BigDecimal totalExpenses, BigDecimal expectedCash) {
        this.openingAmount = openingAmount;
        this.cashSales = cashSales;
        this.totalExpenses = totalExpenses;
        this.expectedCash = expectedCash;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public BigDecimal getCashSales() {
        return cashSales;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }
}
