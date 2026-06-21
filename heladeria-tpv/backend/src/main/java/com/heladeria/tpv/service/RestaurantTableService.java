package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.RestaurantTableRequest;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.Product;
import com.heladeria.tpv.model.RestaurantTable;
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

    public RestaurantTableService(RestaurantTableRepository tableRepository, ProductRepository productRepository) {
        this.tableRepository = tableRepository;
        this.productRepository = productRepository;
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

    /**
     * Suma el precio del producto al total pendiente de la mesa.
     * No se guarda detalle de que producto fue, solo el monto.
     */
    @Transactional
    public RestaurantTable addProduct(Long tableId, Long productId, int quantity) {
        RestaurantTable table = findById(tableId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        BigDecimal addition = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        table.setPendingTotal(table.getPendingTotal().add(addition));
        return tableRepository.save(table);
    }

    /**
     * Resetea el pendiente a cero, normalmente despues de cobrar.
     * La mesa sigue existiendo con el mismo nombre.
     */
    @Transactional
    public RestaurantTable resetPending(Long tableId) {
        RestaurantTable table = findById(tableId);
        table.setPendingTotal(BigDecimal.ZERO);
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
