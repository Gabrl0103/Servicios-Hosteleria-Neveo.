package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.PendingTableItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PendingTableItemRepository extends JpaRepository<PendingTableItem, Long> {

    List<PendingTableItem> findByTableId(Long tableId);

    Optional<PendingTableItem> findByTableIdAndProductId(Long tableId, Long productId);

    void deleteByTableId(Long tableId);
}
