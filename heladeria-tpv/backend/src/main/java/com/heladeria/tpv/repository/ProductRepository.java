package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAvailableTrue();
}
