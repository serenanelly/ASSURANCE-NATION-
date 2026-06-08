package com.assurance.nation.controller;

import com.assurance.nation.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(Constants.API_V1)
@RequiredArgsConstructor
@Tag(name = "Santé", description = "Contrôle de disponibilité de l'API")
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Vérifie API, base de données et espace disque (public)")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", "ASSURANCE NATION");
        body.put("version", "1.0.0");
        body.put("timestamp", Instant.now().toString());
        body.put("database", checkDatabase());
        body.put("disk", checkDisk());
        return ResponseEntity.ok(body);
    }

    private String checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            return "DOWN";
        }
    }

    private String checkDisk() {
        File root = new File(".");
        long usable = root.getUsableSpace();
        return usable > 10_000_000L ? "UP" : "DOWN";
    }
}
