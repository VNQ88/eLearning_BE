package org.example.elearningbe.payment.order.dto;

import java.util.List;

public record OrderResponse(
        Long id,
        float totalAmount,
        String status,
        List<OrderItemResponse> items
) {}
