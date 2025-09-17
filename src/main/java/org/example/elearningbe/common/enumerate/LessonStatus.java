package org.example.elearningbe.common.enumerate;

public enum LessonStatus {
    PENDING_RESOURCE,   // đã tạo DB nhưng resource chưa promote xong
    ACTIVE,             // promote thành công, lesson sẵn sàng
    ARCHIVED           // bài học bị ẩn, không còn sử dụng
}
