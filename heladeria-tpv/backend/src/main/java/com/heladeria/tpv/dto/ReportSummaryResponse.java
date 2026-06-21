package com.heladeria.tpv.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReportSummaryResponse {

    private BigDecimal totalAmount;
    private long totalOrders;
    private BigDecimal previousPeriodAmount;
    private Double percentChangeVsPrevious; // null si el periodo anterior fue 0
    private BigDecimal averageTicket;
    private DailySalesPoint bestDay; // null si no hay ventas
    private List<DailySalesPoint> dailySales;
    private List<ProductRankingItem> topProducts;
    private PaymentMethodReportResponse paymentBreakdown;

    public ReportSummaryResponse(BigDecimal totalAmount, long totalOrders,
                                  BigDecimal previousPeriodAmount, Double percentChangeVsPrevious,
                                  BigDecimal averageTicket, DailySalesPoint bestDay,
                                  List<DailySalesPoint> dailySales, List<ProductRankingItem> topProducts,
                                  PaymentMethodReportResponse paymentBreakdown) {
        this.totalAmount = totalAmount;
        this.totalOrders = totalOrders;
        this.previousPeriodAmount = previousPeriodAmount;
        this.percentChangeVsPrevious = percentChangeVsPrevious;
        this.averageTicket = averageTicket;
        this.bestDay = bestDay;
        this.dailySales = dailySales;
        this.topProducts = topProducts;
        this.paymentBreakdown = paymentBreakdown;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getPreviousPeriodAmount() {
        return previousPeriodAmount;
    }

    public Double getPercentChangeVsPrevious() {
        return percentChangeVsPrevious;
    }

    public BigDecimal getAverageTicket() {
        return averageTicket;
    }

    public DailySalesPoint getBestDay() {
        return bestDay;
    }

    public List<DailySalesPoint> getDailySales() {
        return dailySales;
    }

    public List<ProductRankingItem> getTopProducts() {
        return topProducts;
    }

    public PaymentMethodReportResponse getPaymentBreakdown() {
        return paymentBreakdown;
    }
}
