package org.example.elearningbe.payment.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal totalAmount,
        String status,
        List<OrderItemResponse> items
) {}
