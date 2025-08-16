package org.example.elearningbe.course.dto;

public record CourseResponse(
        Long id,
        String title,
        String description,
        String image,
        float price,
        Integer duration,
        String ownerEmail
){

}
