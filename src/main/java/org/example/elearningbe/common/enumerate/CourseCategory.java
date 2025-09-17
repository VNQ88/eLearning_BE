package org.example.elearningbe.common.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CourseCategory {
    PROGRAMMING,
    DESIGN,
    MARKETING,
    BUSINESS,
    LANGUAGES,
    MUSIC,
    PHOTOGRAPHY,
    FINANCE,
    ENGINEERING,
    TEACHING,
    OTHER;

    @JsonCreator
    public static CourseCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        // chuẩn hóa: bỏ khoảng trắng, thay space -> underscore, rồi uppercase
//        String normalized = value.trim().toUpperCase().replace(" ", "_");
        return CourseCategory.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(); // serialize về lowercase
    }
}
