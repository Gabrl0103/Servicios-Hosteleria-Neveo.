package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.Order;
import com.heladeria.tpv.model.OrderStatus;
import com.heladeria.tpv.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCashRegisterIdAndStatus(Long cashRegisterId, OrderStatus status);

    @Query("""
        select o.paymentMethod as paymentMethod,
               sum(o.total) as total,
               count(o) as orderCount
        from Order o
        where o.status = com.heladeria.tpv.model.OrderStatus.CONFIRMADO
          and o.createdAt >= :from
          and o.createdAt < :to
        group by o.paymentMethod
        """)
    List<PaymentMethodTotalsProjection> sumTotalsByPaymentMethodBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        select o.paymentMethod as paymentMethod,
               sum(o.total) as total,
               count(o) as orderCount
        from Order o
        where o.status = com.heladeria.tpv.model.OrderStatus.CONFIRMADO
          and o.cashRegister.id = :cashRegisterId
        group by o.paymentMethod
        """)
    List<PaymentMethodTotalsProjection> sumTotalsByPaymentMethodForCashRegister(
            @Param("cashRegisterId") Long cashRegisterId);

    long countByCashRegisterIdAndStatus(Long cashRegisterId, OrderStatus status);

    @Query("""
        select o
        from Order o
        where o.status = com.heladeria.tpv.model.OrderStatus.CONFIRMADO
          and o.createdAt >= :from
          and o.createdAt < :to
        order by o.createdAt asc
        """)
    List<Order> findConfirmedBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    interface PaymentMethodTotalsProjection {
        PaymentMethod getPaymentMethod();
        java.math.BigDecimal getTotal();
        Long getOrderCount();
    }
}
