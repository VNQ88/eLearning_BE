package org.example.elearningbe.payment.cart.dto;

import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        float totalAmount
) {}
