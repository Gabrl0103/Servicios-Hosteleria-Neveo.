package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.CashierRequest;
import com.heladeria.tpv.model.Cashier;
import com.heladeria.tpv.service.CashierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cashiers")
public class CashierController {

    private final CashierService cashierService;

    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping
    public List<Cashier> findAll() {
        return cashierService.findAll();
    }

    @PostMapping
    public Cashier create(@Valid @RequestBody CashierRequest request) {
        return cashierService.create(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cashierService.delete(id);
    }
}
