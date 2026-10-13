package com.example.demo.controllers;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ViolatingOrderProcessingService {

	private final OrderRepository orderRepository;
	private final NotificationService notificationService;

	public ViolatingOrderProcessingService(OrderRepository orderRepository, NotificationService notificationService) {
		this.orderRepository = orderRepository;
		this.notificationService = notificationService;
	}

	/**
	 * ❌ VIOLATIONS TRIGGERED IN THIS METHOD: 1. [AG020] Long Parameter List: 6
	 * parameters (>= 5 threshold) 2. [AG020] Deep Nesting / Arrow Antipattern: 5
	 * levels of indentation (>= 4 threshold) 3. [AG020] Monster Method Length: 70+
	 * lines (> 60 lines threshold) 4. [AG018] High Cyclomatic Complexity:
	 * Complexity > 16 (> 15 threshold) 5. [AG019] Algorithmic Performance Smells: -
	 * List.contains() lookup inside a loop: O(n^2) scan -
	 * orderRepository.findById() inside a loop: N+1 remote DB roundtrips
	 */
	public void processOrderBatch(String customerId, String regionCode, String currency, boolean isVip, int maxRetries,
			List<String> orderIdList) { // ❌ AG020: 6 parameters (Long Parameter List)

		if (customerId != null && !customerId.isBlank()) { // Level 1 Nesting
			if (regionCode != null
					&& (regionCode.equals("US") || regionCode.equals("EU") || regionCode.equals("APAC"))) { // Level 2
				for (String orderId : orderIdList) { // Level 3 Loop
					if (orderId != null && !orderId.startsWith("TEMP_")) { // Level 4
						try { // Level 5 (Deep Nesting Violation)

							// ❌ AG019: DB Query in loop (N+1 database roundtrips)
							Order order = orderRepository.findById(orderId).orElse(null);

							if (order != null) {
								// ❌ AG019: O(n^2) Linear scan on List inside loop
								List<String> blacklistedItems = List.of("SKU_99", "SKU_88", "SKU_77");
								if (blacklistedItems.contains(order.getItemSku())) {
									order.setStatus("REJECTED_BLACKLISTED");
								} else if (order.getAmount() > 10000 && isVip) {
									order.setStatus("APPROVED_EXPEDITED");
									order.setDiscountRate(0.15);
								} else if (order.getAmount() > 5000) {
									if (currency.equals("USD") || currency.equals("EUR")) {
										order.setStatus("APPROVED_STANDARD");
										order.setDiscountRate(0.05);
									} else {
										order.setStatus("APPROVED_CONVERTED");
										order.setDiscountRate(0.02);
									}
								} else if (order.getAmount() > 1000 && maxRetries > 2) {
									order.setStatus("PENDING_VERIFICATION");
								} else if (order.getAmount() <= 0) {
									order.setStatus("INVALID_AMOUNT");
								} else {
									order.setStatus("PROCESSED_DEFAULT");
								}
								// ❌ AG019: DB Save in loop
								orderRepository.save(order);
							}
						} catch (Exception e) {
							notificationService.alertFailure(orderId, e.getMessage());
						}
					}
				}
			} else {
				throw new IllegalArgumentException("Unsupported region: " + regionCode);
			}
		} else {
			throw new IllegalArgumentException("Invalid customer ID");
		}
	}
}