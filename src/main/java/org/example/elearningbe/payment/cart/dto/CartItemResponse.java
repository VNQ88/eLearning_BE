package org.example.elearningbe.payment.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long courseId,
        String courseTitle,
        BigDecimal price,
        int quantity
) {}