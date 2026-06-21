package com.heladeria.tpv.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShiftReceiptResponse {

    private Long cashRegisterId;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private boolean open;
    private long totalSales;
    private long voidedSales;
    private String cashierName;
    private String businessName;
    private String nit;
    private String address;
    private String phone;
    private String logoBase64;
    private PaymentMethodReportResponse paymentBreakdown;
    private BigDecimal openingAmount;
    private BigDecimal cashSales;
    private BigDecimal totalExpenses;
    private List<ExpenseResponse> expenses;
    private BigDecimal expectedCash;

    public ShiftReceiptResponse(Long cashRegisterId, LocalDateTime openedAt, LocalDateTime closedAt,
                                 long totalSales, long voidedSales,
                                 String cashierName,
                                 String businessName, String nit, String address, String phone,
                                 String logoBase64,
                                 PaymentMethodReportResponse paymentBreakdown,
                                 BigDecimal openingAmount, BigDecimal cashSales,
                                 BigDecimal totalExpenses, List<ExpenseResponse> expenses,
                                 BigDecimal expectedCash) {
        this.cashRegisterId = cashRegisterId;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.open = closedAt == null;
        this.totalSales = totalSales;
        this.voidedSales = voidedSales;
        this.cashierName = cashierName;
        this.businessName = businessName;
        this.nit = nit;
        this.address = address;
        this.phone = phone;
        this.logoBase64 = logoBase64;
        this.paymentBreakdown = paymentBreakdown;
        this.openingAmount = openingAmount;
        this.cashSales = cashSales;
        this.totalExpenses = totalExpenses;
        this.expenses = expenses;
        this.expectedCash = expectedCash;
    }

    public Long getCashRegisterId() {
        return cashRegisterId;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public boolean isOpen() {
        return open;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public long getVoidedSales() {
        return voidedSales;
    }

    public String getCashierName() {
        return cashierName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getNit() {
        return nit;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public PaymentMethodReportResponse getPaymentBreakdown() {
        return paymentBreakdown;
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

    public List<ExpenseResponse> getExpenses() {
        return expenses;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }
}
