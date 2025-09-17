package org.example.elearningbe.common.enumerate;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum LessonType {
    VIDEO, DOCUMENT;

    @JsonCreator
    public static LessonType from(String value) {
        if (value == null) return null;
        try {
            return LessonType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid lesson type: " + value);
        }
    }
}

