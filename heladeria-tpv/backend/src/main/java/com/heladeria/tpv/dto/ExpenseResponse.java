package com.heladeria.tpv.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public ExpenseResponse(Long id, String description, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
