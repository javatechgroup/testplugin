package com.example.demo.controllers;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class CompliantOrderProcessingService {

    private static final Set<String> ALLOWED_REGIONS = Set.of("US", "EU", "APAC");
    private static final Set<String> BLACKLISTED_SKUS = Set.of("SKU_99", "SKU_88", "SKU_77");

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public CompliantOrderProcessingService(OrderRepository orderRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    // ✅ AG020 Fix: Encapsulate 6 parameters into a Java 21 Record
    public record BatchContext(
            String customerId,
            String regionCode,
            String currency,
            boolean isVip,
            int maxRetries
    ) {}

    public void processOrderBatch(BatchContext ctx, List<String> orderIdList) {
        // ✅ AG020 Fix: Guard Clauses / Return Early (Flattens nesting to Level 1)
        validateContext(ctx);

        // ✅ AG019 Fix: Single batch database query outside loop
        List<Order> orders = orderRepository.findAllById(orderIdList);

        for (Order order : orders) {
            evaluateOrderStatus(order, ctx);
        }

        // ✅ AG019 Fix: Batch save outside loop
        orderRepository.saveAll(orders);
    }

    private void validateContext(BatchContext ctx) {
        if (ctx.customerId() == null || ctx.customerId().isBlank()) {
            throw new IllegalArgumentException("Invalid customer ID");
        }
        if (!ALLOWED_REGIONS.contains(ctx.regionCode())) {
            throw new IllegalArgumentException("Unsupported region: " + ctx.regionCode());
        }
    }

    // ✅ AG018 & AG020 Fix: Extracted small cohesive helper method (< 15 lines, CC < 5)
    private void evaluateOrderStatus(Order order, BatchContext ctx) {
        if (BLACKLISTED_SKUS.contains(order.getItemSku())) { // ✅ O(1) Set lookup
            order.setStatus("REJECTED_BLACKLISTED");
            return;
        }

        if (order.getAmount() > 10000 && ctx.isVip()) {
            order.setStatus("APPROVED_EXPEDITED");
            order.setDiscountRate(0.15);
        } else if (order.getAmount() > 5000) {
            order.setStatus("APPROVED_STANDARD");
            order.setDiscountRate(0.05);
        } else {
            order.setStatus("PROCESSED_DEFAULT");
        }
    }
}