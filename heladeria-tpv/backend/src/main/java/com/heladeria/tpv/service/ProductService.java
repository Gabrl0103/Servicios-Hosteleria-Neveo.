package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.ProductRequest;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.Product;
import com.heladeria.tpv.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllAvailable() {
        return productRepository.findByAvailableTrue();
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product(request.getName(), request.getCategory(), request.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        return productRepository.save(product);
    }

    @Transactional
    public Product setAvailability(Long id, boolean available) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        product.setAvailable(available);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado");
        }
        productRepository.deleteById(id);
    }
}
