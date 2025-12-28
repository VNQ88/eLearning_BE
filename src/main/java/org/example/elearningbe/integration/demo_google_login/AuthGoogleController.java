package org.example.elearningbe.integration.demo_google_login;

// package org.example.elearningbe.auth.controller;

import lombok.RequiredArgsConstructor;

import org.example.elearningbe.integration.demo_google_login.dto.AuthResponse;
import org.example.elearningbe.integration.demo_google_login.dto.GoogleLoginRequest;
import org.example.elearningbe.integration.demo_google_login.dto.GoogleUserInfo;
import org.example.elearningbe.security.JwtService;
import org.example.elearningbe.user.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/android")
public class AuthGoogleController {

    private final GoogleTokenVerifierServiceAndroid googleTokenVerifierService;
    private final SocialAuthUserService socialAuthUserService;
    private final JwtService jwtService;

    @Value("${application.jwt.valid-duration}")
    private long validDurationSeconds;

    @Value("${application.jwt.refreshable-duration}")
    private long refreshDurationSeconds;

    @PostMapping("/google")
    public AuthResponse loginWithGoogleAndroid(@RequestBody GoogleLoginRequest request) {
        if (request.getIdToken() == null || request.getIdToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "idToken is required");
        }

        GoogleUserInfo googleUser = googleTokenVerifierService.verify(request.getIdToken());
        if (googleUser == null || !googleUser.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
        }

        User user = socialAuthUserService.findOrCreateFromGoogle(googleUser);

        long accessMs = validDurationSeconds * 1000L;
        long refreshMs = refreshDurationSeconds * 1000L;

        String accessToken = jwtService.generateToken(user, accessMs);
        String refreshToken = jwtService.generateToken(user, refreshMs);

        AuthResponse data = new AuthResponse(accessToken, refreshToken);
        return data;
    }
}
