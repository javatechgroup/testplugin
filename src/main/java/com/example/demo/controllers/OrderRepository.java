package com.example.demo.controllers;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    // ❌ AG004: Repository to Service Inverted Dependency
    private final TestService testService;

    public OrderRepository(TestService testService) {
        this.testService = testService;
    }

    public Optional<Order> findById(String id) {
        return Optional.of(new Order(id, "SKU_1", 100.0));
    }
    public List<Order> findAllById(Iterable<String> ids) {
        return List.of();
    }
    public Order save(Order order) {
        return order;
    }
    public List<Order> saveAll(Iterable<Order> orders) {
        return List.of();
    }
}