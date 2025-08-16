package org.example.elearningbe.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.Serializable;



@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CourseRequest implements Serializable {
        @NotBlank(message = "Title is required")
        String title;
        @NotBlank(message = "Description is required")
        String description;
        @NotBlank(message = "Image is required")
        String image;
        Float price;
        Integer duration;
        @Nullable
        String ownerEmail;
}
