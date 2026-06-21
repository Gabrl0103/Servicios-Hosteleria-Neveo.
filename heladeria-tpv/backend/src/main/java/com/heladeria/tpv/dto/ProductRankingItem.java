package com.heladeria.tpv.dto;

import java.math.BigDecimal;

public class ProductRankingItem {

    private Long productId;
    private String productName;
    private String category;
    private long totalQuantity;
    private BigDecimal totalAmount;

    public ProductRankingItem(Long productId, String productName, String category,
                               long totalQuantity, BigDecimal totalAmount) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
