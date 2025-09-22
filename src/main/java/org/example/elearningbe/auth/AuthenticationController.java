package org.example.elearningbe.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.elearningbe.auth.dto.AuthenticationRequest;
import org.example.elearningbe.auth.dto.RegistrationRequest;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.common.respone.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Authentication Controller")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "User login",
            description = "Xác thực người dùng bằng email/password và trả về access token + refresh token."
    )
    @PostMapping("/authenticate")
    public ResponseData<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Authentication successful",
                authenticationService.authenticate(authenticationRequest));
    }

    @Operation(
            summary = "User registration",
            description = "Đăng ký tài khoản mới. Sau khi đăng ký thành công, hệ thống sẽ gửi email kích hoạt."
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> register(@RequestBody @Valid RegistrationRequest registrationRequest) throws MessagingException {
        authenticationService.register(registrationRequest);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Registration successful. Please check your email to activate your account.");
    }

    @Operation(
            summary = "Refresh token",
            description = "Dùng refresh token để lấy access token mới."
    )
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> refreshToken(HttpServletRequest request) {
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Token refreshed successfully",
                authenticationService.refreshToken(request));
    }

    @Operation(
            summary = "Logout",
            description = "Đăng xuất người dùng hiện tại, vô hiệu hóa refresh token."
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Logout successful");
    }

    @Operation(
            summary = "Activate account",
            description = "Kích hoạt tài khoản bằng mã code đã được gửi qua email."
    )
    @PostMapping("/activate-account")
    public ResponseData<?> confirmAccount(@RequestParam String code) throws MessagingException {
        try {
            authenticationService.activateAccount(code);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Account activated.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("expired")) {
                authenticationService.resendActivationCode(code);
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            }
            throw e;
        }
    }
}
