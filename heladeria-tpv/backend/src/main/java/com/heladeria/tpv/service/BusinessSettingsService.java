package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.BusinessSettingsRequest;
import com.heladeria.tpv.model.BusinessSettings;
import com.heladeria.tpv.repository.BusinessSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessSettingsService {

    private final BusinessSettingsRepository repository;

    public BusinessSettingsService(BusinessSettingsRepository repository) {
        this.repository = repository;
    }

    public BusinessSettings get() {
        return repository.findById(1L).orElseGet(() -> {
            BusinessSettings defaults = new BusinessSettings(
                    1L, "Heladeria", "901.234.567-8", "Calle 12 # 34-56, Bogota", "310 123 4567");
            return repository.save(defaults);
        });
    }

    @Transactional
    public BusinessSettings update(BusinessSettingsRequest request) {
        BusinessSettings settings = get();
        settings.setBusinessName(request.getBusinessName());
        settings.setNit(request.getNit());
        settings.setAddress(request.getAddress());
        settings.setPhone(request.getPhone());
        settings.setLogoBase64(request.getLogoBase64());
        settings.setBusinessHours(request.getBusinessHours());
        settings.setInstagramHandle(request.getInstagramHandle());
        settings.setFacebookUrl(request.getFacebookUrl());
        settings.setWhatsappNumber(request.getWhatsappNumber());
        return repository.save(settings);
    }
}
