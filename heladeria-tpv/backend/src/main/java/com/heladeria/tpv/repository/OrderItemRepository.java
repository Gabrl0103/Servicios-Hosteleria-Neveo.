package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        select i
        from OrderItem i
        where i.order.status = com.heladeria.tpv.model.OrderStatus.CONFIRMADO
          and i.order.createdAt >= :from
          and i.order.createdAt < :to
        """)
    List<OrderItem> findConfirmedItemsBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
