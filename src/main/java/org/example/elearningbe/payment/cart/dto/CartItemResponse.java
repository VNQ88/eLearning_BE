package org.example.elearningbe.payment.cart.dto;

public record CartItemResponse(
        Long id,
        Long courseId,
        String courseTitle,
        float price,
        int quantity
) {}