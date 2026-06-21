package com.heladeria.tpv.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_registers")
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "opened_by_id", nullable = false)
    private User openedBy;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column(nullable = false)
    private String cashierName;

    @Column(precision = 12, scale = 2)
    private BigDecimal openingAmount;

    @Column
    private LocalDateTime closedAt;

    public CashRegister() {
    }

    public CashRegister(User openedBy, String cashierName, BigDecimal openingAmount) {
        this.openedBy = openedBy;
        this.cashierName = cashierName;
        this.openingAmount = openingAmount != null ? openingAmount : BigDecimal.ZERO;
        this.openedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return closedAt == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(User openedBy) {
        this.openedBy = openedBy;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
