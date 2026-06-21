package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.ProductRequest;
import com.heladeria.tpv.model.Product;
import com.heladeria.tpv.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> findAll(@RequestParam(defaultValue = "false") boolean onlyAvailable) {
        return onlyAvailable ? productService.findAllAvailable() : productService.findAll();
    }

    @PostMapping
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/{id}/availability")
    public Product setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return productService.setAvailability(id, available);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
