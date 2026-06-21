package com.heladeria.tpv.service;

import com.heladeria.tpv.exception.BusinessRuleException;
import com.heladeria.tpv.exception.ResourceNotFoundException;
import com.heladeria.tpv.model.CashRegister;
import com.heladeria.tpv.model.User;
import com.heladeria.tpv.repository.CashRegisterRepository;
import com.heladeria.tpv.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CashRegisterService {

    private final CashRegisterRepository cashRegisterRepository;
    private final UserRepository userRepository;

    public CashRegisterService(CashRegisterRepository cashRegisterRepository, UserRepository userRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.userRepository = userRepository;
    }

    public CashRegister getOpenRegisterOrThrow() {
        return cashRegisterRepository.findByClosedAtIsNull()
                .orElseThrow(() -> new BusinessRuleException("No hay un turno de caja abierto"));
    }

    /**
     * Historial completo de turnos, mas reciente primero.
     * Usado por la pantalla de Cuadre de caja.
     */
    public java.util.List<CashRegister> findAll() {
        return cashRegisterRepository.findAllByOrderByOpenedAtDesc();
    }

    @Transactional
    public CashRegister openRegister(Long userId, String cashierName, BigDecimal openingAmount) {
        if (cashierName == null || cashierName.isBlank()) {
            throw new BusinessRuleException("Debes seleccionar un cajero");
        }

        cashRegisterRepository.findByClosedAtIsNull().ifPresent(open -> {
            throw new BusinessRuleException("Ya hay un turno abierto, cierralo antes de abrir uno nuevo");
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        CashRegister register = new CashRegister(user, cashierName, openingAmount);
        return cashRegisterRepository.save(register);
    }

    @Transactional
    public CashRegister closeRegister(Long registerId) {
        CashRegister register = cashRegisterRepository.findById(registerId)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado"));

        if (!register.isOpen()) {
            throw new BusinessRuleException("Este turno ya esta cerrado");
        }

        register.setClosedAt(LocalDateTime.now());
        return cashRegisterRepository.save(register);
    }
}
