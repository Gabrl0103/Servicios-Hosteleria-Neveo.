package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.CashRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByClosedAtIsNull();
    List<CashRegister> findAllByOrderByOpenedAtDesc();
}
