package org.example.elearningbe.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.elearningbe.common.enumerate.CourseCategory;
import org.example.elearningbe.validator.EnumValue;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;


@Setter                 // <—
@NoArgsConstructor
@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CourseRequest implements Serializable {
        @NotBlank(message = "Title is required")
        String title;
        @NotBlank(message = "Description is required")
        String description;
        @NotNull(message = "Image is required")
        MultipartFile image;
        @NotNull @PositiveOrZero
        Float price;
        @NotBlank(message = "Category is required")
        @EnumValue(name = "category", enumClass = CourseCategory.class, message = "Category must be one of the defined categories")
        String category;
        @Nullable
        Integer duration;
        @Nullable
        String ownerEmail;
}
