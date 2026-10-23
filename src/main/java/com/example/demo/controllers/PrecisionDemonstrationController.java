package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Tests precision fixes for REST APIs:
 * 1. AG012: Direct Entity Exposure - UserAccount lacks 'Entity' suffix but has @Entity, so it MUST be flagged.
 * 2. AG015: Missing Validation - byte[], String, and Map payloads do NOT require @Valid and must NOT be flagged.
 */
@RestController
@RequestMapping("/api/precision")
public class PrecisionDemonstrationController {

    // AG012 Test: Exposing UserAccount (detected via @Entity discovery despite no 'Entity' suffix) -> FLAGGED
    @GetMapping("/account")
    public UserAccount getAccount() {
        return new UserAccount();
    }

    // AG015 Test: Raw binary payload -> Compliant (no false positive for @Valid)
    @PostMapping("/upload")
    public void uploadRawData(@RequestBody byte[] rawBytes) {
        // No @Valid required on raw binary arrays
    }

    // AG015 Test: Raw string webhook payload -> Compliant (no false positive for @Valid)
    @PostMapping("/webhook")
    public void receiveWebhook(@RequestBody String rawPayload) {
        // No @Valid required on raw strings
    }

    // AG015 Test: Untyped dynamic map -> Compliant (no false positive for @Valid)
    @PostMapping("/dynamic")
    public void handleDynamicJson(@RequestBody Map<String, Object> dynamicMap) {
        // No @Valid required on raw Map
    }
}
