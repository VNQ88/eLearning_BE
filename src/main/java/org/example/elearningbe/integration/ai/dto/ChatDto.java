package org.example.elearningbe.integration.ai.dto;

/** Bản ghi lịch sử chat (kèm thời gian) */
public record ChatDto(
        String role,              // user | assistant | system
        String text           // nội dung
) {}


