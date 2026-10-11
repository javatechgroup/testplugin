package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/demo-controllers")
public class ViolatingArchitectureController {

    // ❌ AG001: Controller to Repository Direct Dependency
    private final OrderRepository orderRepository;

    // ❌ AG002: Controller to Controller Direct Dependency
    private final TestController testController;

    // ❌ AG007: Prohibited Field Injection
    @Autowired
    private NotificationService autowiredNotificationService;

    public ViolatingArchitectureController(OrderRepository orderRepository, TestController testController) {
        this.orderRepository = orderRepository;
        this.testController = testController;
    }

    // ❌ AG012: Direct JPA Entity Exposure in REST Controller
    @GetMapping("/entity/{id}")
    public ResponseEntity<DemoCustomerEntity> getCustomerEntity(@PathVariable Long id) {
        return ResponseEntity.ok(new DemoCustomerEntity());
    }

    // ❌ AG015: Missing @Valid on @RequestBody mutation parameter
    @PostMapping("/customers")
    public ResponseEntity<String> createCustomer(@RequestBody DemoCustomerDto request) {
        return ResponseEntity.ok("Created");
    }

    // ❌ AG016: Unpaginated REST collection return in @GetMapping
    @GetMapping("/unpaginated-list")
    public List<String> getAllCustomerNames() {
        return List.of("Alice", "Bob");
    }

    public static class DemoCustomerDto {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
