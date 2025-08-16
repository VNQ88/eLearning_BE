package org.example.elearningbe.common.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CourseCategory {
    PROGRAMMING,
    DATA_SCIENCE,
    DESIGN,
    MARKETING,
    BUSINESS,
    LANGUAGES,
    PERSONAL_DEVELOPMENT,
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
        return CourseCategory.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(); // serialize về lowercase
    }
}
