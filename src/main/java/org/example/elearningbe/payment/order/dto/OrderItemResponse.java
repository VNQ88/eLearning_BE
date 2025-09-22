package org.example.elearningbe.payment.order.dto;

public record OrderItemResponse(
        Long id,
        Long courseId,
        String courseTitle,
        float priceAtPurchase
) {}
