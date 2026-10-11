package com.example.demo.controllers;

import jakarta.persistence.*;

@Entity
@Table(name = "demo_orders")
public class DemoOrderEntity {

    @Id
    private Long id;

    private String description;

    public DemoOrderEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
