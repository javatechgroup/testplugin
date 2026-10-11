package com.example.demo.controllers;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sample Spring Service demonstrating enterprise quality checks:
 * - AG021: Spring Self-Invocation Proxy Bypass (@Transactional / @Async called via this.method())
 * - AG022: Singleton Mutable Instance State (SimpleDateFormat, non-final counter, mutable List)
 * - AG023: NullPointer Traps (Optional.get(), return null collection, inverted equals)
 * - AG024: AutoCloseable Resource Leak (FileInputStream outside try-with-resources)
 * - AG025: Inefficient String Concatenation in Loop (+= in loop)
 */
@Service
public class ViolatingEnterpriseQualityService {

    // =========================================================================
    // AG022: Singleton Bean Mutable Instance State (Race Conditions in @Service)
    // =========================================================================
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd"); // ❌ Non-thread-safe formatter!
    private List<String> activeBatchRequests = new ArrayList<>(); // ❌ Mutable state shared across concurrent requests!
    private int processedOrderCounter = 0; // ❌ Non-final state variable!

    // =========================================================================
    // AG021: Spring Proxy Self-Invocation Bypass
    // =========================================================================
    public void processOrderBatch(List<String> orderIds) {
        for (String orderId : orderIds) {
            // ❌ Self-invocation: calls internal @Transactional method directly, bypassing Spring AOP proxy!
            executeTransactionalOrderSave(orderId);
            // ❌ Self-invocation: calls internal @Async method directly!
            dispatchAsyncNotification(orderId);
        }
    }

    @Transactional
    public void executeTransactionalOrderSave(String orderId) {
        // Save order to DB with transactional rollback expectation
        processedOrderCounter++;
    }

    @Async
    public void dispatchAsyncNotification(String orderId) {
        // Send notification asynchronously
    }

    // =========================================================================
    // AG023: NullPointer Traps & Defensive Coding Antipatterns
    // =========================================================================
    public List<String> getOrderHistory(String userRole, Optional<String> optionalUserId) {
        // ❌ 1. Inverted String equals (Throws NullPointerException if userRole is null)
        if (userRole.equals("ADMIN")) {
            // ❌ 2. Unchecked Optional.get() without presence check
            String userId = optionalUserId.get(); 
            activeBatchRequests.add(userId);
        }

        // ❌ 3. Returning null from collection-returning method (causes downstream NPEs in callers)
        if (activeBatchRequests.isEmpty()) {
            return null; 
        }

        return activeBatchRequests;
    }

    // =========================================================================
    // AG024: AutoCloseable Resource Leak (Unclosed I/O Stream)
    // =========================================================================
    public byte[] loadConfigurationFile(File configFile) throws IOException {
        // ❌ Unclosed FileInputStream created outside try-with-resources statement!
        FileInputStream fis = new FileInputStream(configFile);
        byte[] buffer = fis.readAllBytes();
        fis.close();
        return buffer;
    }

    // =========================================================================
    // AG025: Inefficient String Concatenation in Loops (O(n^2) Heap Allocations)
    // =========================================================================
    public String buildOrderAuditLogCsv(List<String> auditLogs) {
        String csvReport = "ID,TIMESTAMP,ACTION\n";
        for (String log : auditLogs) {
            // ❌ Repeated String concatenation inside loop causes high GC thrashing
            csvReport += log + "\n"; 
        }
        return csvReport;
    }

    // =========================================================================
    // AG026: Remote I/O Inside @Transactional Boundary (HikariCP Starvation)
    // =========================================================================
    @Transactional
    public void executeRemotePaymentInTransaction(java.net.URI endpoint) throws Exception {
        // Remote HTTP call inside @Transactional locks DB connection while waiting for network!
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder().uri(endpoint).build();
        httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
    }

    // =========================================================================
    // AG027: Java 21 Virtual Thread Pinning (synchronized method and block)
    // =========================================================================
    public synchronized String getCachedExchangeRateSyncMethod(String currency) {
        // Synchronized method pins OS carrier thread in Project Loom
        return "1.25";
    }

    public void processCriticalTransactionBlock(String txId) {
        // Synchronized block pins OS carrier thread in Project Loom
        synchronized (this) {
            processedOrderCounter++;
        }
    }

    // =========================================================================
    // AG028: ThreadLocal Memory Leak & Context Contamination
    // =========================================================================
    private static final ThreadLocal<String> userSecurityContext = new ThreadLocal<>();

    public void processUserRequestWithLeak(String userId) {
        // Leaks into worker thread pool because remove() is never called in finally block!
        userSecurityContext.set(userId);
        processedOrderCounter++;
    }
}