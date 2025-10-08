package org.example.elearningbe.integration.ai.dto;

/** Bản ghi lịch sử chat (kèm thời gian) */
public record HistoryItemDto(
        String role,              // user | assistant | system
        String text           // nội dung
) {}


