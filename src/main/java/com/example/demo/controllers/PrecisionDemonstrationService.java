package com.example.demo.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tests precision fixes for services and algorithms:
 * 1. AG008: Catch clause with 'ignored' is NOT flagged.
 * 2. AG019: String getter .contains() and .indexOf() in loops are NOT flagged as O(n^2).
 * 3. AG020: Method with 25 lines of code + 45 lines of documentation comments is NOT flagged as Monster Method.
 * 4. AG027 (Safe): ReentrantLock properly released in finally block is NOT flagged.
 * 5. AG027 (Unsafe): ReentrantLock acquired without finally release IS flagged as ERROR (deadlock hazard).
 * 6. AG030: RestTemplateBuilder.build() without timeout configuration IS flagged.
 */
@Service
public class PrecisionDemonstrationService {

    private final ReentrantLock safeLock = new ReentrantLock();
    private final ReentrantLock unsafeLock = new ReentrantLock();

    // 1. AG008 Test: Deliberately ignored exception per Java convention -> Compliant (NOT FLAGGED)
    public int parseIntegerSafely(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    // 2. AG019 Test: String getters in loop -> Compliant (NOT FLAGGED as O(n^2) collection lookup)
    public void filterUserAccounts(List<UserAccount> accounts) {
        for (UserAccount account : accounts) {
            if (account.getEmail() != null && account.getEmail().contains("@company.com")) {
                // String search on getter, not collection scan
            }
            if (account.getUsername() != null && account.getUsername().indexOf('_') >= 0) {
                // String indexOf, not collection scan
            }
        }
    }

    // 3. AG020 Test: Clean 25-line method with 45 lines of helpful comments -> Compliant (NOT FLAGGED as > 60 lines)
    public int computeMetricsWithRichDocumentation(int base) {
        // Step 1: Initialize the tracking accumulator
        // Architectural Note: This method documents the exact calculation steps
        // to explain the revenue reconciliation workflow to maintenance engineers.
        // Documenting line 1
        // Documenting line 2
        // Documenting line 3
        // Documenting line 4
        // Documenting line 5
        // Documenting line 6
        // Documenting line 7
        // Documenting line 8
        // Documenting line 9
        // Documenting line 10
        // Documenting line 11
        // Documenting line 12
        // Documenting line 13
        // Documenting line 14
        // Documenting line 15
        // Documenting line 16
        // Documenting line 17
        // Documenting line 18
        // Documenting line 19
        // Documenting line 20
        // Documenting line 21
        // Documenting line 22
        // Documenting line 23
        // Documenting line 24
        // Documenting line 25
        // Documenting line 26
        // Documenting line 27
        // Documenting line 28
        // Documenting line 29
        // Documenting line 30
        int result = base;
        result += 1;
        result += 2;
        result += 3;
        result += 4;
        result += 5;
        result += 6;
        result += 7;
        result += 8;
        result += 9;
        result += 10;
        result += 11;
        result += 12;
        result += 13;
        result += 14;
        result += 15;
        return result;
    }

    // 4. AG027 Test (Safe): Standard ReentrantLock pattern -> Compliant (NOT FLAGGED)
    public void executeUnderSafeLock() {
        safeLock.lock();
        try {
            System.out.println("Executing thread-safe operation");
        } finally {
            safeLock.unlock();
        }
    }

    // 5. AG027 Test (Unsafe): Lock acquired without try-finally -> FLAGGED as ERROR (Unreleased Lock)
    public void executeUnderUnsafeLock() {
        unsafeLock.lock();
        System.out.println("Dangerous: Missing finally block!");
        unsafeLock.unlock();
    }

    // 6. AG030 Test: RestClient.builder().build() without timeouts -> FLAGGED as WARNING
    public org.springframework.web.client.RestClient createUnconfiguredRestClient() {
        return org.springframework.web.client.RestClient.builder().build();
    }
}
