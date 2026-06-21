package com.heladeria.tpv.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Mesa simple, sin concepto de apertura/cierre. Solo tiene un nombre y
 * un monto pendiente de pago que sube cuando se agregan productos sin
 * cobrar de inmediato, y vuelve a cero cuando se cobra. La mesa nunca
 * se "libera": sigue existiendo con el mismo nombre indefinidamente,
 * hasta que alguien la elimine manualmente.
 */
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pendingTotal = BigDecimal.ZERO;

    @Column
    private Double positionX;

    @Column
    private Double positionY;

    public RestaurantTable() {
    }

    public RestaurantTable(String name) {
        this.name = name;
        this.pendingTotal = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPendingTotal() {
        return pendingTotal;
    }

    public void setPendingTotal(BigDecimal pendingTotal) {
        this.pendingTotal = pendingTotal;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }
}
