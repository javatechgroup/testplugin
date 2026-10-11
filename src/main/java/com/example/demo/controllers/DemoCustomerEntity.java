package com.example.demo.controllers;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "demo_customers")
public class DemoCustomerEntity {

    @Id
    private Long id;

    private String name;

    // ❌ AG014: JPA Eager Fetch Detection
    @OneToMany(fetch = FetchType.EAGER)
    private List<DemoOrderEntity> orders;

    public DemoCustomerEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<DemoOrderEntity> getOrders() { return orders; }
    public void setOrders(List<DemoOrderEntity> orders) { this.orders = orders; }
}
