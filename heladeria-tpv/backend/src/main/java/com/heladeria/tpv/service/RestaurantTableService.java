package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.RestaurantTableRequest;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.PendingTableItem;
import com.heladeria.tpv.model.Product;
import com.heladeria.tpv.model.RestaurantTable;
import com.heladeria.tpv.repository.PendingTableItemRepository;
import com.heladeria.tpv.repository.ProductRepository;
import com.heladeria.tpv.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final ProductRepository productRepository;
    private final PendingTableItemRepository pendingItemRepository;

    public RestaurantTableService(RestaurantTableRepository tableRepository,
                                  ProductRepository productRepository,
                                  PendingTableItemRepository pendingItemRepository) {
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
        this.pendingItemRepository = pendingItemRepository;
    }

    public List<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    public RestaurantTable findById(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));
    }

    @Transactional
    public RestaurantTable create(RestaurantTableRequest request) {
        RestaurantTable table = new RestaurantTable(request.getName());
        return tableRepository.save(table);
    }

    @Transactional
    public RestaurantTable rename(Long id, RestaurantTableRequest request) {
        RestaurantTable table = findById(id);
        table.setName(request.getName());
        return tableRepository.save(table);
    }

    @Transactional
    public void delete(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mesa no encontrada");
        }
        tableRepository.deleteById(id);
    }

    @Transactional
    public RestaurantTable addProduct(Long tableId, Long productId, int quantity) {
        RestaurantTable table = findById(tableId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        BigDecimal addition = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        table.setPendingTotal(table.getPendingTotal().add(addition));

        PendingTableItem item = pendingItemRepository.findByTableIdAndProductId(tableId, productId)
                .orElse(null);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new PendingTableItem(table, product, quantity);
        }
        pendingItemRepository.save(item);

        return tableRepository.save(table);
    }

    @Transactional
    public RestaurantTable removeProduct(Long tableId, Long productId, int quantity) {
        RestaurantTable table = findById(tableId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        BigDecimal subtraction = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal newTotal = table.getPendingTotal().subtract(subtraction);
        table.setPendingTotal(newTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newTotal);

        PendingTableItem item = pendingItemRepository.findByTableIdAndProductId(tableId, productId)
                .orElse(null);
        if (item != null) {
            int remaining = item.getQuantity() - quantity;
            if (remaining <= 0) {
                pendingItemRepository.delete(item);
            } else {
                item.setQuantity(remaining);
                pendingItemRepository.save(item);
            }
        }

        return tableRepository.save(table);
    }

    public List<PendingTableItem> getPendingItems(Long tableId) {
        findById(tableId);
        return pendingItemRepository.findByTableId(tableId);
    }

    @Transactional
    public RestaurantTable resetPending(Long tableId) {
        RestaurantTable table = findById(tableId);
        table.setPendingTotal(BigDecimal.ZERO);
        pendingItemRepository.deleteByTableId(tableId);
        return tableRepository.save(table);
    }

    @Transactional
    public RestaurantTable updatePosition(Long id, Double positionX, Double positionY) {
        RestaurantTable table = findById(id);
        table.setPositionX(positionX);
        table.setPositionY(positionY);
        return tableRepository.save(table);
    }
}
