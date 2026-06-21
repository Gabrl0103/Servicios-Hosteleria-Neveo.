package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCashRegisterId(Long cashRegisterId);
}
