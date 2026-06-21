package com.heladeria.tpv.repository;

import com.heladeria.tpv.model.BusinessSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSettingsRepository extends JpaRepository<BusinessSettings, Long> {
}
