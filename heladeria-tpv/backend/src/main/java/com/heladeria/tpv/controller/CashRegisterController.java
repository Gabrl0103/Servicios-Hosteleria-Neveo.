package com.heladeria.tpv.controller;

import com.heladeria.tpv.model.CashRegister;
import com.heladeria.tpv.service.CashRegisterService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cash-registers")
public class CashRegisterController {

    private final CashRegisterService cashRegisterService;

    public CashRegisterController(CashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    @GetMapping("/current")
    public CashRegister getCurrent() {
        return cashRegisterService.getOpenRegisterOrThrow();
    }

    @GetMapping
    public java.util.List<CashRegister> findAll() {
        return cashRegisterService.findAll();
    }

    @PostMapping("/open")
    public CashRegister open(@RequestParam Long userId, @RequestParam String cashierName,
                             @RequestParam(required = false) BigDecimal openingAmount) {
        return cashRegisterService.openRegister(userId, cashierName, openingAmount);
    }

    @PostMapping("/{id}/close")
    public CashRegister close(@PathVariable Long id) {
        return cashRegisterService.closeRegister(id);
    }
}
