package com.heladeria.tpv.service;

import com.heladeria.tpv.dto.BusinessSettingsRequest;
import com.heladeria.tpv.model.BusinessSettings;
import com.heladeria.tpv.repository.BusinessSettingsRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
public class BusinessSettingsService {

    private static final Logger log = LoggerFactory.getLogger(BusinessSettingsService.class);

    private final BusinessSettingsRepository repository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${APP_DATA_DIR:.}")
    private String appDataDir;

    public BusinessSettingsService(BusinessSettingsRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrateLogoIfNeeded() {
        try {
            String oldBase64 = jdbcTemplate.queryForObject(
                    "SELECT logo_base64 FROM business_settings WHERE id = 1 AND logo_base64 IS NOT NULL",
                    String.class);
            if (oldBase64 != null) {
                BusinessSettings settings = repository.findById(1L).orElse(null);
                if (settings != null && settings.getLogoPath() == null) {
                    String path = saveLogoFromDataUrl(oldBase64);
                    settings.setLogoPath(path);
                    repository.save(settings);
                    log.info("Migrated logo from DB column to filesystem: {}", path);
                }
            }
        } catch (Exception e) {
            // Column absent (fresh install) or already migrated — safe to ignore
        }
    }

    public BusinessSettings get() {
        BusinessSettings settings = repository.findById(1L).orElseGet(() -> {
            BusinessSettings defaults = new BusinessSettings(
                    1L, "Heladeria", "901.234.567-8", "Calle 12 # 34-56, Bogota", "310 123 4567");
            return repository.save(defaults);
        });
        if (settings.getLogoPath() != null) {
            settings.setLogoBase64(loadLogoAsDataUrl(settings.getLogoPath()));
        }
        return settings;
    }

    @Transactional
    public BusinessSettings update(BusinessSettingsRequest request) {
        BusinessSettings settings = get();
        settings.setBusinessName(request.getBusinessName());
        settings.setNit(request.getNit());
        settings.setAddress(request.getAddress());
        settings.setPhone(request.getPhone());
        settings.setBusinessHours(request.getBusinessHours());
        settings.setInstagramHandle(request.getInstagramHandle());
        settings.setFacebookUrl(request.getFacebookUrl());
        settings.setWhatsappNumber(request.getWhatsappNumber());

        String incomingLogo = request.getLogoBase64();
        if (incomingLogo == null || incomingLogo.isBlank()) {
            settings.setLogoPath(null);
        } else if (incomingLogo.startsWith("data:")) {
            try {
                String path = saveLogoFromDataUrl(incomingLogo);
                settings.setLogoPath(path);
            } catch (IOException e) {
                log.error("Failed to save logo file", e);
            }
        }
        // If incoming is already a data URL we already handled it above.
        // logoBase64 is @Transient so won't be persisted; re-populate after save.
        BusinessSettings saved = repository.save(settings);
        if (saved.getLogoPath() != null) {
            saved.setLogoBase64(loadLogoAsDataUrl(saved.getLogoPath()));
        }
        return saved;
    }

    private String saveLogoFromDataUrl(String dataUrl) throws IOException {
        int commaIdx = dataUrl.indexOf(',');
        String header = dataUrl.substring(0, commaIdx);
        String base64Data = dataUrl.substring(commaIdx + 1);

        String mimeType = header.replace("data:", "").replace(";base64", "");
        String ext = mimeType.contains("png") ? "png" : mimeType.contains("gif") ? "gif" : "jpg";

        byte[] bytes = Base64.getDecoder().decode(base64Data);

        File uploadsDir = new File(appDataDir, "uploads");
        uploadsDir.mkdirs();
        File[] existing = uploadsDir.listFiles(f -> f.getName().startsWith("logo."));
        if (existing != null) {
            for (File f : existing) f.delete();
        }
        File logoFile = new File(uploadsDir, "logo." + ext);
        Files.write(logoFile.toPath(), bytes);

        return "uploads/logo." + ext;
    }

    private String loadLogoAsDataUrl(String logoPath) {
        File logoFile = new File(appDataDir, logoPath);
        if (!logoFile.exists()) return null;
        try {
            byte[] bytes = Files.readAllBytes(logoFile.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String ext = logoPath.substring(logoPath.lastIndexOf('.') + 1).toLowerCase();
            String mime = ext.equals("png") ? "image/png" : ext.equals("gif") ? "image/gif" : "image/jpeg";
            return "data:" + mime + ";base64," + base64;
        } catch (IOException e) {
            log.error("Failed to read logo file: {}", logoPath, e);
            return null;
        }
    }
}
