package org.example.elearningbe.integration.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import okhttp3.OkHttpClient;

import java.net.URI;
import java.time.Duration;

@EnableConfigurationProperties(MinioProps.class)
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProps props;

    @Bean
    public MinioClient minioClient() {
        // 1) Lấy endpoint từ cấu hình (CHỈ scheme://host:port)
        String raw = props.getEndpoint();

        // 2) Sanitize: nếu có path/query/fragment -> loại bỏ (tránh lỗi "no path allowed")
        URI u = URI.create(raw);
        String safeEndpoint = u.getScheme() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "");

        // 3) OkHttp timeouts (có thể tuỳ chỉnh)
        OkHttpClient http = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .callTimeout(Duration.ofMinutes(5))
                .build();

        // 4) Tạo MinioClient
        return MinioClient.builder()
                .endpoint(safeEndpoint)
                .credentials(props.getAccessKey(), props.getSecretKey())
                .httpClient(http)
                .build();
    }
}
