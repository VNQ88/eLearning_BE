package org.example.elearningbe.redis.resetToken;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("ResetToken")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetToken implements Serializable {
    private String id;
    private String token; // The reset token
    private String userEmail; // Email of the user for whom the reset token is generated
    private long expireTime; // Expiration time in milliseconds
}
