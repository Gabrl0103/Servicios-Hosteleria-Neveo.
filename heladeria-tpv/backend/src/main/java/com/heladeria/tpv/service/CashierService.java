package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.CashierRequest;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.Cashier;
import com.heladeria.tpv.repository.CashierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CashierService {

    private final CashierRepository cashierRepository;

    public CashierService(CashierRepository cashierRepository) {
        this.cashierRepository = cashierRepository;
    }

    public List<Cashier> findAll() {
        return cashierRepository.findAll();
    }

    public Cashier create(CashierRequest request) {
        Cashier cashier = new Cashier(request.getName());
        return cashierRepository.save(cashier);
    }

    public void delete(Long id) {
        if (!cashierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cajero no encontrado");
        }
        cashierRepository.deleteById(id);
    }
}
