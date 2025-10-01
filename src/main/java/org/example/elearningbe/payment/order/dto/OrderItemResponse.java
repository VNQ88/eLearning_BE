package org.example.elearningbe.payment.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long courseId,
        String courseTitle,
        BigDecimal priceAtPurchase
) {}
