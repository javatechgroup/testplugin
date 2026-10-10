package com.example.demo.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CompliantEnterpriseQualityService {

    // ✅ Thread-safe immutable formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // ✅ Safe defensive coding: Yoda condition, safe Optional handling, returns empty collection
    public List<String> getOrderHistorySafe(String userRole, Optional<String> optionalUserId) {
        if ("ADMIN".equals(userRole)) {
            String userId = optionalUserId.orElseThrow(() -> new IllegalArgumentException("User ID required"));
            return List.of(userId);
        }
        return Collections.emptyList(); // Safe empty collection
    }

    // ✅ Try-with-resources guarantees automatic OS handle closure
    public byte[] loadConfigurationFileSafe(File configFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            return fis.readAllBytes();
        }
    }

    // ✅ StringBuilder prevents O(n^2) heap allocations
    public String buildOrderAuditLogCsvSafe(List<String> auditLogs) {
        StringBuilder csvReport = new StringBuilder("ID,TIMESTAMP,ACTION\n");
        for (String log : auditLogs) {
            csvReport.append(log).append("\n");
        }
        return csvReport.toString();
    }
}