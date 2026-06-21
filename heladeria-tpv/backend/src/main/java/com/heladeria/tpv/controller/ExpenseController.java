package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.ExpenseRequest;
import com.heladeria.tpv.dto.ExpenseResponse;
import com.heladeria.tpv.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<ExpenseResponse> findByCashRegister(@RequestParam Long cashRegisterId) {
        return expenseService.findByCashRegister(cashRegisterId);
    }

    @PostMapping
    public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
        return expenseService.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        expenseService.delete(id);
    }
}
