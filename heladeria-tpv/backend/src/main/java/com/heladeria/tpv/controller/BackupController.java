package com.heladeria.tpv.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Value("${APP_DATA_DIR:.}")
    private String appDataDir;

    @GetMapping("/database")
    public ResponseEntity<Resource> downloadDatabase() {
        File dbFile = new File(appDataDir, "heladeria.db");

        if (!dbFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        String filename = "heladeria-backup-" + LocalDate.now() + ".db";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(new FileSystemResource(dbFile));
    }
}
