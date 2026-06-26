package com.heladeria.tpv.controller;

import com.heladeria.tpv.service.BackupMaintenanceService;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    private static final Logger log = LoggerFactory.getLogger(BackupController.class);

    @Value("${APP_DATA_DIR:.}")
    private String appDataDir;

    private final DataSource dataSource;
    private final BackupMaintenanceService maintenanceService;

    public BackupController(DataSource dataSource, BackupMaintenanceService maintenanceService) {
        this.dataSource = dataSource;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/database")
    public ResponseEntity<Resource> downloadDatabase() {
        File dbFile = new File(appDataDir, "heladeria.db");
        if (!dbFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
        } catch (Exception ignored) {
        }
        String filename = "heladeria-backup-" + LocalDate.now() + ".db";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(dbFile));
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listBackups() {
        File backupsDir = new File(appDataDir, "backups");
        if (!backupsDir.exists()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        File[] files = backupsDir.listFiles(
                f -> f.getName().startsWith("heladeria-backup-") && f.getName().endsWith(".db"));
        if (files == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<String> names = Arrays.stream(files)
                .map(File::getName)
                .sorted(Collections.reverseOrder())
                .toList();
        return ResponseEntity.ok(names);
    }

    @PostMapping("/maintenance/run")
    public ResponseEntity<Map<String, String>> runMaintenance() {
        maintenanceService.runMaintenance();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restoreFromBackup(@RequestParam String name) {
        File backupsDir = new File(appDataDir, "backups");
        File source = new File(backupsDir, name);
        if (!source.exists() || !source.getParentFile().getAbsolutePath().equals(backupsDir.getAbsolutePath())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Backup no encontrado: " + name));
        }
        return performRestore(source);
    }

    @PostMapping("/restore/upload")
    public ResponseEntity<Map<String, String>> restoreFromUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        }
        try {
            File tempFile = File.createTempFile("restore-upload-", ".db");
            file.transferTo(tempFile);
            ResponseEntity<Map<String, String>> response = performRestore(tempFile);
            tempFile.deleteOnExit();
            return response;
        } catch (IOException e) {
            log.error("Failed to save uploaded restore file", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al procesar archivo: " + e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, String>> performRestore(File source) {
        File dbFile = new File(appDataDir, "heladeria.db");
        File walFile = new File(appDataDir, "heladeria.db-wal");
        File shmFile = new File(appDataDir, "heladeria.db-shm");
        File backupsDir = new File(appDataDir, "backups");
        backupsDir.mkdirs();

        // Pre-restore backup of current state
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
        File preRestoreBackup = new File(backupsDir, "heladeria-prerestore-" + timestamp + ".db");
        try {
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA wal_checkpoint(TRUNCATE)");
            }
            if (dbFile.exists()) {
                Files.copy(dbFile.toPath(), preRestoreBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("Restore: pre-restore backup saved at {}", preRestoreBackup.getName());
            }
        } catch (Exception e) {
            log.error("Restore: failed to create pre-restore backup", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No se pudo crear respaldo previo: " + e.getMessage()));
        }

        // Close pool to release Windows file locks before replacing the file
        try {
            ((HikariDataSource) dataSource).close();
            log.info("Restore: HikariCP pool closed");
        } catch (Exception e) {
            log.warn("Restore: could not close datasource cleanly", e);
        }

        // Replace db file and clean WAL/SHM
        try {
            Files.copy(source.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(walFile.toPath());
            Files.deleteIfExists(shmFile.toPath());
            log.info("Restore: database replaced from {}", source.getName());
        } catch (IOException e) {
            log.error("Restore: failed to replace database file", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al reemplazar la base de datos: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "status", "restart_required",
                "message", "Restauración completada. Cierra y vuelve a abrir la aplicación para aplicar los cambios.",
                "preRestoreBackup", preRestoreBackup.getName()));
    }
}
