package com.example.demo.controllers;

public class Order {
    private String id;
    private String itemSku;
    private double amount;
    private String status;
    private double discountRate;

    public Order() {}

    public Order(String id, String itemSku, double amount) {
        this.id = id;
        this.itemSku = itemSku;
        this.amount = amount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemSku() { return itemSku; }
    public void setItemSku(String itemSku) { this.itemSku = itemSku; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
}