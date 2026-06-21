package com.heladeria.tpv.dto;

import com.heladeria.tpv.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {

    @NotNull
    private Long userId;

    // Mesa de la cual se cobra. Siempre presente en el flujo actual,
    // pero se deja opcional por flexibilidad futura.
    private Long tableId;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    @NotNull
    private PaymentMethod paymentMethod;

    // Solo aplica cuando paymentMethod es EFECTIVO
    private BigDecimal amountReceived;

    // Porcentaje de descuento manual, ej 10 = 10%. Opcional.
    @DecimalMin("0")
    @DecimalMax("100")
    private BigDecimal discountPercent;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    public static class OrderItemRequest {

        @NotNull
        private Long productId;

        @NotNull
        @Positive
        private Integer quantity;

        // Observacion libre para este producto, ej "sin azucar"
        private String note;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
