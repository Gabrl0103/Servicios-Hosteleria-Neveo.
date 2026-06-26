package com.heladeria.tpv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class BackupMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(BackupMaintenanceService.class);
    private static final int MAX_BACKUPS = 30;

    @Value("${APP_DATA_DIR:.}")
    private String appDataDir;

    private final DataSource dataSource;

    public BackupMaintenanceService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Scheduled(initialDelay = 60_000, fixedRate = 3_600_000)
    public void runIfDueToday() {
        File lastRunFile = new File(appDataDir, "maintenance-last-run.txt");
        if (lastRunFile.exists()) {
            try {
                String lastRun = Files.readString(lastRunFile.toPath()).trim();
                if (LocalDate.parse(lastRun).isEqual(LocalDate.now())) {
                    return;
                }
            } catch (Exception e) {
                // corrupted file — run anyway
            }
        }
        runMaintenance();
    }

    public void runMaintenance() {
        log.info("Starting scheduled database maintenance");
        File dbFile = new File(appDataDir, "heladeria.db");
        File backupsDir = new File(appDataDir, "backups");
        backupsDir.mkdirs();

        String dateStr = LocalDate.now().toString();
        File backupFile = new File(backupsDir, "heladeria-backup-" + dateStr + ".db");

        // Step 1: WAL checkpoint + backup (must succeed before VACUUM)
        try {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            }
            Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Maintenance: backup created at {}", backupFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Maintenance: backup step failed — aborting", e);
            return;
        }

        // Step 2: integrity_check — stop if not ok
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA integrity_check");
            String result = rs.next() ? rs.getString(1) : "error";
            if (!"ok".equalsIgnoreCase(result)) {
                log.error("Maintenance: integrity_check returned '{}' — skipping VACUUM, backup preserved", result);
                writeLastRun();
                return;
            }
        } catch (Exception e) {
            log.error("Maintenance: integrity_check failed", e);
            return;
        }

        // Step 3: VACUUM
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM");
            log.info("Maintenance: VACUUM completed");
        } catch (Exception e) {
            log.error("Maintenance: VACUUM failed", e);
            return;
        }

        // Step 4: ANALYZE
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("ANALYZE");
            log.info("Maintenance: ANALYZE completed");
        } catch (Exception e) {
            log.error("Maintenance: ANALYZE failed", e);
            return;
        }

        // Step 5: rotate backups
        try {
            rotateBackups(backupsDir);
        } catch (Exception e) {
            log.error("Maintenance: backup rotation failed", e);
        }

        writeLastRun();
        log.info("Maintenance completed successfully");
    }

    private void rotateBackups(File backupsDir) {
        File[] backups = backupsDir.listFiles(
                f -> f.getName().startsWith("heladeria-backup-") && f.getName().endsWith(".db"));
        if (backups == null || backups.length <= MAX_BACKUPS) return;
        Arrays.sort(backups, Comparator.comparing(File::getName));
        int toDelete = backups.length - MAX_BACKUPS;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                log.info("Maintenance: deleted old backup {}", backups[i].getName());
            }
        }
    }

    private void writeLastRun() {
        File lastRunFile = new File(appDataDir, "maintenance-last-run.txt");
        try {
            Files.writeString(lastRunFile.toPath(), LocalDate.now().toString());
        } catch (IOException e) {
            log.error("Maintenance: failed to write last-run file", e);
        }
    }
}
