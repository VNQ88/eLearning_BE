package org.example.elearningbe.course.dto;

import java.math.BigDecimal;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String image,
        String category,
        BigDecimal price,
        Integer duration,
        String ownerEmail,
        String ownerAvatar,
        String ownerName
){

}
