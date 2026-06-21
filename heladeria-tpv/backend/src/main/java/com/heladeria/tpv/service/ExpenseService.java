package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.ExpenseRequest;
import com.heladeria.tpv.dto.ExpenseResponse;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.CashRegister;
import com.heladeria.tpv.model.Expense;
import com.heladeria.tpv.repository.CashRegisterRepository;
import com.heladeria.tpv.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CashRegisterRepository cashRegisterRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CashRegisterRepository cashRegisterRepository) {
        this.expenseRepository = expenseRepository;
        this.cashRegisterRepository = cashRegisterRepository;
    }

    public List<ExpenseResponse> findByCashRegister(Long cashRegisterId) {
        return expenseRepository.findByCashRegisterId(cashRegisterId).stream()
                .map(e -> new ExpenseResponse(e.getId(), e.getDescription(), e.getAmount(), e.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public ExpenseResponse create(ExpenseRequest request) {
        CashRegister register = cashRegisterRepository.findById(request.getCashRegisterId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));
        Expense expense = new Expense(register, request.getDescription(), request.getAmount());
        expense = expenseRepository.save(expense);
        return new ExpenseResponse(expense.getId(), expense.getDescription(), expense.getAmount(), expense.getCreatedAt());
    }

    public void delete(Long expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new ResourceNotFoundException("Gasto no encontrado");
        }
        expenseRepository.deleteById(expenseId);
    }
}
