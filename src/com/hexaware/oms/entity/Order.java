package com.hexaware.oms.entity;

import java.time.LocalDateTime;

public class Order {

    private int orderId;
    private int userId;
    private LocalDateTime orderDate;

    public Order() {
    }

    public Order(int orderId, int userId, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}
