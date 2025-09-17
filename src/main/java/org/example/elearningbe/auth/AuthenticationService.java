package org.example.elearningbe.auth;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.elearningbe.auth.dto.AuthenticationRequest;
import org.example.elearningbe.auth.dto.AuthenticationResponse;
import org.example.elearningbe.auth.dto.RegistrationRequest;
import org.example.elearningbe.exception.InvalidDataException;
import org.example.elearningbe.integration.mail.EmailService;
import org.example.elearningbe.integration.mail.EmailTemplateName;
import org.example.elearningbe.integration.redis.RedisToken;
import org.example.elearningbe.integration.redis.RedisTokenService;
import org.example.elearningbe.role.RoleRepository;
import org.example.elearningbe.security.JwtService;
import org.example.elearningbe.security.UserDetailServiceImpl;
import org.example.elearningbe.user.UserRepository;
import org.example.elearningbe.user.entities.User;
import org.example.elearningbe.user.verification_code.VerificationCode;
import org.example.elearningbe.user.verification_code.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.thymeleaf.exceptions.TemplateInputException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserDetailServiceImpl userDetailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;

    @Value("${application.jwt.valid-duration}")
    private int validDuration;
    @Value("${application.jwt.refreshable-duration}")
    private int refreshDuration;
    @Value("${application.mailing.front-end.activation-url}")
    private String activationUrl;
    @Value("${application.mailing.front-end.verification-code-length}")
    private int verificationCodeLength;
    @Value("${application.mailing.front-end.verification-code-expiration-minutes}")
    private int verificationCodeExpirationMinutes;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        var user = (User) auth.getPrincipal();
        var accessToken = jwtService.generateToken(user, (long) validDuration * 1000);
        var refreshToken = jwtService.generateToken(user, (long) refreshDuration * 1000);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional(rollbackOn = {MessagingException.class, TemplateInputException.class})
    public void register(RegistrationRequest registrationRequest) throws MessagingException {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            var existingUser = userDetailService.loadUserByUsername(registrationRequest.getEmail());
            if (existingUser.isEnabled()) {
                throw new RuntimeException("User with this email already exists and is activated");
            } else {
                sendValidationEmail((User) existingUser);
            }
            return;
        }

        var userRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Role STUDENT was not initialized"));
        var user = User.builder()
                .email(registrationRequest.getEmail())
                .password(registrationRequest.getPassword())
                .fullName(registrationRequest.getFullName())
                .enabled(false)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newCode = generateAndSaveActivationCode(user);

        emailService.sendEmail(user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newCode,
                "Activate your account");
    }

    private String generateAndSaveActivationCode(User user) {
        String generatedCode = generateActivationCode(verificationCodeLength);
        var token = VerificationCode.builder()
                .code(generatedCode)
                .user(user)
                .createAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(verificationCodeExpirationMinutes))
                .build();
        verificationCodeRepository.save(token);
        return generatedCode;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }

    @Transactional
    public void activateAccount(String code) {
        VerificationCode savedToken = verificationCodeRepository.findByCode(code.strip())
                .orElseThrow(() -> new RuntimeException("Code not found"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiredAt())) {
            throw new RuntimeException("Activation token has expired. A new token has been sent to your email.");
        }
        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        verificationCodeRepository.save(savedToken);
    }

    public void resendActivationCode(String code) throws MessagingException {
        String email = verificationCodeRepository.findByCode(code)
                .map(token -> token.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("Cannot find user for code"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        sendValidationEmail(user);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        log.info("---------- refreshToken ----------");

        final String refreshToken = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }
        if (redisTokenService.isRefreshTokenRevoked(refreshToken)) {
            throw new InvalidDataException("Refresh token has been revoked");
        }
        final String userName = jwtService.extractUsername(refreshToken);
        var user = userDetailService.loadUserByUsername(userName);
        if (jwtService.isTokenValid(refreshToken, user)) {
            throw new InvalidDataException("Not allow access with this token");
        }

        // Tạo accessToken và refreshToken mới
        String newAccessToken = jwtService.generateToken(user, (long) validDuration * 1000);
        String newRefreshToken = jwtService.generateToken(user, (long) refreshDuration * 1000);

        // Vô hiệu hóa refreshToken cũ
        redisTokenService.save(RedisToken.builder()
                .id(refreshToken)
                .accessToken(null)
                .refreshToken(refreshToken)
                .expireTime(jwtService.extractExpiration(refreshToken))
                .build());

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(HttpServletRequest request) {
        log.info("---------- logout ----------");
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new InvalidDataException("Access token must not be blank");
        }
        final String accessToken = authHeader.substring(7);
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Refresh token must not be blank");
        }

        final String userName = jwtService.extractUsername(accessToken);
        var user = userDetailService.loadUserByUsername(userName);
        if (jwtService.isTokenValid(accessToken, user)) {
            throw new InvalidDataException("Invalid access token");
        }
        if (jwtService.isTokenValid(refreshToken, user)) {
            throw new InvalidDataException("Invalid refresh token");
        }

        // Lưu accessToken và refreshToken vào Redis với id là accessToken
        redisTokenService.save(RedisToken.builder()
                .id(accessToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expireTime(jwtService.extractExpiration(refreshToken))
                .build());
    }

//    public String forgotPassword(String email) {
//        log.info("---------- forgotPassword ----------");
//
//        // check email exists or not
//        User user = userService.getUserByEmail(email);
//
//        // generate reset token
//        String resetToken = jwtService.generateResetToken(user);
//
//        // save to db
//        // tokenService.save(Token.builder().username(user.getUsername()).resetToken(resetToken).build());
//        redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());
//
//        // TODO send email to user
//        String confirmLink = String.format("curl --location 'http://localhost:80/auth/reset-password' \\\n" +
//                "--header 'accept: */*' \\\n" +
//                "--header 'Content-Type: application/json' \\\n" +
//                "--data '%s'", resetToken);
//        log.info("--> confirmLink: {}", confirmLink);
//
//        return resetToken;
//    }
}