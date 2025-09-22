package org.example.elearningbe.integration.minio;

import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.integration.minio.dto.PresignPutRequest;
import org.example.elearningbe.integration.minio.dto.UploadResult;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
@Tag(name = "Storage Controller")
public class StorageController {

    private final MinioChannel minioChannel;
    private final MinioProps minioProps;

    /* ------------ 1) Upload trực tiếp qua backend (đơn giản) ------------ */
    @Operation(summary = "Upload file", description = "Upload file directly via backend (simple, but not recommended for large files)")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) throws Exception {
        var result = minioChannel.upload(file); // giờ trả UploadResult(objectKey, url)
        return ResponseEntity.ok(Map.of(
                "objectKey", result.objectKey(),
                "url", result.url(),
                "ttlSeconds", minioProps.getPresignExpirySeconds()
        ));
    }

    /* ------------ 2) Presign PUT: Lấy link upload tạm ------------ */
    @Operation(summary = "Presign PUT URL", description = "Get a presigned PUT URL for direct upload to MinIO/S3")
    @PostMapping("/put")
    public ResponseData<?> presignPut(@Valid @RequestBody PresignPutRequest req) throws Exception {
        String ext = getExtension(req.getContentType());
        String objectKey;

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        switch (req.getResourceType()) {
            case AVATAR ->
                // userId bạn có thể lấy từ principal thay vì username
                objectKey = String.format("avatars/%s/avatar.%s", username, ext);
            case LESSON -> {
                if (req.getCourseId() == null) {
                    throw new IllegalArgumentException("courseId is required for LESSON resource type");
                }
                String uuid = UUID.randomUUID().toString();
                objectKey = String.format("uploads/courses/%d/%s.%s", req.getCourseId(), uuid, ext);
            }
            default -> {
                String uuid = UUID.randomUUID().toString();
                objectKey = String.format("uploads/other/%s.%s", uuid, ext);
            }
        }

        String uploadUrl = minioChannel.presignedPutUrl(objectKey, req.getTtlSeconds(), req.getContentType());

        return new ResponseData<>(HttpStatus.OK.value(), "Presign success",
                new UploadResult(objectKey, uploadUrl));
    }

    /** Helper để lấy đuôi file từ contentType */
    private String getExtension(String contentType) {
        if (contentType == null) return "dat";
        return switch (contentType) {
            case "video/mp4" -> "mp4";
            case "application/pdf" -> "pdf";
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> "dat";
        };
    }

    /* ------------ 3) Presign GET: Lấy link tải tạm ------------ */
    @GetMapping("/presign/get")
    @Operation(summary = "Presign GET URL", description = "Get a presigned GET URL for using to load/use file from MinIO/S3")
    public ResponseEntity<?> presignGet(@RequestParam("key") String objectKey,
                                        @RequestParam(value = "ttl", required = false) Integer ttlSeconds) throws Exception {
        int ttl = (ttlSeconds != null && ttlSeconds > 0) ? ttlSeconds : minioProps.getPresignExpirySeconds();
        String url = minioChannel.presignedGetUrl(objectKey, ttl);
        return ResponseEntity.ok(Map.of(
                "url", url,
                "ttlSeconds", ttl
        ));
    }

    /* ------------ 4) Download qua backend (ít dùng cho file lớn) ------------ */
    @GetMapping("/download/stream")
    @Operation(summary = "Download file stream", description = "Download file stream via backend (not recommended for large files)")
    public ResponseEntity<InputStreamResource> downloadStream(@RequestParam("key") String objectKey) throws Exception {
        GetObjectResponse stream = minioChannel.downloadStream(minioProps.getBucket(), objectKey);

        String filename = objectKey.contains("/")
                ? objectKey.substring(objectKey.lastIndexOf('/') + 1)
                : objectKey;

        // Content-Disposition để browser tự động tải file
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/video/stream")
    @Operation(summary = "Stream video with Range support", description = "Stream video files with HTTP Range support for efficient playback")
    public ResponseEntity<Resource> streamVideo(
            @RequestParam("key") String objectKey,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader
    ) throws Exception {
        String bucket = minioProps.getBucket();
        StatObjectResponse stat = minioChannel.statObject(bucket, objectKey);
        long fileSize = stat.size();

        long start = 0;
        long end = fileSize - 1; // mặc định hết file

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.substring(6).split("-");
            start = Long.parseLong(parts[0]);
            if (parts.length > 1 && !parts[1].isBlank()) {
                end = Long.parseLong(parts[1]);
            }
        }

        long chunkSize = (end - start) + 1;
        GetObjectResponse stream = minioChannel.getObjectRange(bucket, objectKey, start, chunkSize);

        InputStreamResource inputStreamResource = new InputStreamResource(stream);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, stat.contentType() != null ? stat.contentType() : "video/mp4");
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(chunkSize));
        headers.set(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, fileSize));

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) // 206
                .headers(headers)
                .body(inputStreamResource);
    }

}
