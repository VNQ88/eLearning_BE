package org.example.elearningbe.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.auth.dto.*;
import org.example.elearningbe.common.respone.ResponseData;
import org.example.elearningbe.common.respone.ResponseError;
import org.example.elearningbe.integration.google.GoogleTokenVerifierService;
import org.example.elearningbe.security.JwtService;
import org.example.elearningbe.security.UserDetailServiceImpl;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Authentication Controller")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final JwtService jwtService;

    @Value("${application.jwt.valid-duration}")
    private long validDuration;
    @Value("${application.jwt.refreshable-duration}")
    private long refreshDuration;

    @Operation(
            summary = "User login",
            description = "Xác thực người dùng bằng email/password và trả về access token + refresh token."
    )
    @PostMapping("/authenticate")
    public ResponseData<?> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest) {
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

    @Operation(
            summary = "Forgot password",
            description = "Gửi email đặt lại mật khẩu cho người dùng."
    )
    @PostMapping("/forgot-password")
    public ResponseData<?> forgotPassword(@RequestParam @NotBlank String email) throws MessagingException {
        return new ResponseData<>(HttpStatus.OK.value(),
                authenticationService.forgotPassword(email));
    }


    @Operation(
            summary = "Verify reset code",
            description = "Xác minh mã code đặt lại mật khẩu đã được gửi qua email."
    )
    @PostMapping("/verify-reset-code")
    public ResponseData<?> verifyResetCode(@RequestBody @Valid VerifyCodeRequest request) throws MessagingException {
        try {
            authenticationService.verifyResetCode(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Code verified successfully");
        }
        catch (RuntimeException e) {
            if (e.getMessage().contains("expired")) {
                authenticationService.resendActivationCode(request.getCode());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            }
            throw e;
        }
    }

    @Operation(
            summary = "Reset password",
            description = "Đặt lại mật khẩu bằng mã code đã được gửi qua email."
    )
    @PostMapping("/reset-password")
    public ResponseData<?> resetPassword(@RequestBody @Valid SetNewPasswordRequest request){
        authenticationService.resetPassword(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Password has been reset successfully");
    }

    @Operation(
            summary = "Login with Google",
            description = "Đăng nhập/đăng ký bằng tài khoản Google."
    )
    @PostMapping("/google")
    public ResponseData<?> loginWithGoogle(@RequestBody GoogleLogin body) throws Exception {
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(body.idToken());

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        log.info("Google payload: {}", payload);
        User user = userRepository.findByEmail(email).orElseGet(
                () -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setAvatar(picture);
                    newUser.setEnabled(true);
                    return newUser;
                }
        );

        // 3. Lưu user vào DB
        user = userRepository.save(user);

        // 4. Sinh JWT access token và refresh token
        UserDetails userDetails = userDetailServiceImpl.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails, validDuration);
        String refreshToken = jwtService.generateToken(userDetails, refreshDuration);

        return new ResponseData<>(HttpStatus.OK.value(), "Login with Google successful",
                AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build());
    }
}
