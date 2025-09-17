package org.example.elearningbe.auth;

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
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/authenticate")
    public ResponseData<?> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest) {
        return new  ResponseData<>(HttpStatus.OK.value(), "Authentication successful",
                authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> register(
            @RequestBody @Valid RegistrationRequest registrationRequest) throws MessagingException {
        authenticationService.register(registrationRequest);
        return new  ResponseData<>(HttpStatus.ACCEPTED.value(), "Registration successful. Please check your email to activate your account.");
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseData<?> refreshToken(HttpServletRequest request) {
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "Token refreshed successfully",
                authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Logout successful");
    }


    @PostMapping("/activate-account")
    public ResponseData<?> confirmAccount(
            @RequestParam String code) throws MessagingException {
        try {
            authenticationService.activateAccount(code);
            return new  ResponseData<>(HttpStatus.ACCEPTED.value(), "Account activated.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("expired")) {
                authenticationService.resendActivationCode(code); // Cần logic trích xuất email
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            }
            throw e;
        }
    }
}
