package org.example.elearningbe.integration.minio;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.integration.minio.dto.UploadResult;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProps.class)
@Slf4j
public class MinioChannel {

    private final MinioProps props;
    private final MinioClient minioClient;

    @PostConstruct
    private void init() {
        createBucketIfNeeded(props.getBucket(), props.isMakeBucketPublic());
    }

    @SneakyThrows
    private void createBucketIfNeeded(final String name, boolean makePublic) {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(name).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
            if (makePublic) {
                final var policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [{
                        "Effect": "Allow",
                        "Principal": "*",
                        "Action": "s3:GetObject",
                        "Resource": "arn:aws:s3:::%s/*"
                      }]
                    }
                    """.formatted(name);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(name).config(policy).build()
                );
                log.warn("Bucket {} đã được đặt PUBLIC (GET). Cân nhắc để private cho an toàn.", name);
            } else {
                log.info("Bucket {} được tạo (private).", name);
            }
        } else {
            log.info("Bucket {} đã tồn tại.", name);
        }
    }

    /** Suy luận MIME type chuẩn từ tên file; fallback octet-stream. */
    private String detectContentType(String filename, String fallback) {
        try {
            String guess = URLConnection.guessContentTypeFromName(filename);
            if (StringUtils.hasText(guess)) return guess;
        } catch (Exception ignored) {}
        return fallback;
    }

    /** Tạo object key duy nhất: {prefix/}{uuid}-{ten-goc} */
    private String buildObjectKey(String originalName) {
        String clean = (originalName == null || originalName.isBlank())
                ? "file.bin"
                : originalName.strip().replace("\\", "/");
        String nameOnly = clean.substring(clean.lastIndexOf('/') + 1);
        String prefix = props.getKeyPrefix() == null ? "" : props.getKeyPrefix().trim();
        if (!prefix.isEmpty() && !prefix.endsWith("/")) prefix += "/";
        return prefix + UUID.randomUUID() + "-" + nameOnly;
    }

    /** Upload file và trả về pre-signed GET URL với TTL cấu hình. */
    // ĐỔI kiểu trả về từ String -> UploadResult
    public UploadResult upload(@NonNull final MultipartFile file) throws Exception {
        final String objectKey = buildObjectKey(file.getOriginalFilename());
        final String contentType = Objects.requireNonNullElse(
                file.getContentType(),
                detectContentType(file.getOriginalFilename(), "application/octet-stream")
        );

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .contentType(contentType)
                            .stream(in, file.getSize(), -1)
                            .build()
            );
        }

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .expiry(props.getPresignExpirySeconds())
                        .build()
        );

        // Trả về cả objectKey + url
        return new UploadResult(objectKey, url);
    }

    public GetObjectResponse downloadStream(String bucket, String objectKey) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }

    /** (Tuỳ chọn) Tạo pre-signed URL GET/PUT với TTL truyền vào. */
    public String presignedGetUrl(String objectKey, int ttlSeconds) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.GET)
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .expiry(ttlSeconds > 0 ? ttlSeconds : props.getPresignExpirySeconds())
                        .build()
        );
    }

    public String presignedPutUrl(String objectKey, int ttlSeconds, String contentType) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.PUT)
                        .bucket(props.getBucket())
                        .object(objectKey)
                        .expiry(ttlSeconds > 0 ? ttlSeconds : props.getPresignExpirySeconds())
                        .extraQueryParams(
                                StringUtils.hasText(contentType)
                                        ? java.util.Map.of("Content-Type", contentType)
                                        : java.util.Collections.emptyMap()
                        )
                        .build()
        );
    }

    public StatObjectResponse statObject(String bucket, String objectKey) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        );
    }

    public GetObjectResponse getObjectRange(String bucket, String objectKey, long start, long length) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .offset(start)
                        .length(length) // -1 = tới EOF
                        .build()
        );
    }

    public void removeObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .build()
            );
            log.info("Removed object {} from bucket {}", objectKey, props.getBucket());
        } catch (Exception e) {
            log.error("Failed to remove object {} from bucket {}", objectKey, props.getBucket(), e);
            throw new RuntimeException("Could not remove object from MinIO", e);
        }
    }


    /** Đọc toàn bộ InputStream → byte[] với buffer 8KB. */
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }
}
