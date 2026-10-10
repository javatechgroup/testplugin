package com.example.demo.controllers;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {
    Optional<Order> findById(String id);
    List<Order> findAllById(Iterable<String> ids);
    Order save(Order order);
    List<Order> saveAll(Iterable<Order> orders);
}