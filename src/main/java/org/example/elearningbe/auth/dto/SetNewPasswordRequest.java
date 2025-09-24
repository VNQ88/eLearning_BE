package org.example.elearningbe.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetNewPasswordRequest {
    @Email
    private String email;
    @NotBlank
    private String code;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}
