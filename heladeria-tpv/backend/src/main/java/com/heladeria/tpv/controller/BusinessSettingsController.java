package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.BusinessSettingsRequest;
import com.heladeria.tpv.model.BusinessSettings;
import com.heladeria.tpv.service.BusinessSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business-settings")
public class BusinessSettingsController {

    private final BusinessSettingsService businessSettingsService;

    public BusinessSettingsController(BusinessSettingsService businessSettingsService) {
        this.businessSettingsService = businessSettingsService;
    }

    @GetMapping
    public BusinessSettings get() {
        return businessSettingsService.get();
    }

    @PutMapping
    public BusinessSettings update(@Valid @RequestBody BusinessSettingsRequest request) {
        return businessSettingsService.update(request);
    }
}
