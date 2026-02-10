package vn.tt.practice.orderservice.enums;

public enum OrderStatus {
    PENDING,           // Order created, waiting for inventory check
    RESERVED,          // Inventory reserved
    PAYMENT_PENDING,   // Waiting for payment
    PAYMENT_PROCESSING,// Payment in progress
    PAID,             // Payment completed
    CONFIRMED,        // Order confirmed
    PROCESSING,       // Being prepared
    SHIPPED,          // Shipped to customer
    DELIVERED,        // Delivered
    CANCELLED,        // Cancelled by user/system
    FAILED           // Order failed
}
