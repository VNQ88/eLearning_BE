package org.example.elearningbe.payment.cart.dto;

import org.example.elearningbe.course.dto.CourseResponse;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        CourseResponse course,
        BigDecimal price,
        int quantity
) {}