package org.example.elearningbe.integration.minio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.elearningbe.common.enumerate.ResourceType;

@Data
public class PresignPutRequest {
    @NotBlank
    private String contentType;   // MIME type: image/png, video/mp4...

    private int ttlSeconds = 3600; // default 60 phút

    @NotNull
    private ResourceType resourceType;

    // Nếu resourceType = LESSON thì cần courseId
    private Long courseId;
}
