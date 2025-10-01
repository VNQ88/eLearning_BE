package org.example.elearningbe.integration.zalopay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.zalo-pay")
public class ZaloPayProps {
    private String appId;

    private String key1;

    private String key2;

    private String endpoint;

    private String callbackUrl;
}
