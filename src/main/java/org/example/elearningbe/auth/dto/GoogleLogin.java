package org.example.elearningbe.auth.dto;

public record GoogleLogin(
        String idToken,
        String deviceId
) {}
