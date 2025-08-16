package org.example.elearningbe.user.verification_code;

import jakarta.persistence.*;
import lombok.*;
import org.example.elearningbe.user.entities.User;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerificationCode {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "token")
    private String code;

    @Column(name = "created_at")
    private LocalDateTime createAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "validatedAt")
    private LocalDateTime validatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
