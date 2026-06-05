package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.AuthResponse;
import com.wasac.utilitybilling.dto.ConfirmOtpRequest;
import com.wasac.utilitybilling.dto.EmailRequest;
import com.wasac.utilitybilling.dto.LoginRequest;
import com.wasac.utilitybilling.dto.ResetPasswordRequest;
import com.wasac.utilitybilling.dto.UserDTO;
import com.wasac.utilitybilling.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody UserDTO request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Signup successful")
                .data(authService.signup(request))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(authService.login(request.getEmail(), request.getPassword()))
                .build());
    }

    @PostMapping("/verify-email/request")
    public ResponseEntity<ApiResponse<String>> requestEmailVerification(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.requestEmailVerification(request));
    }

    @PostMapping("/verify-email/confirm")
    public ResponseEntity<ApiResponse<String>> confirmEmailVerification(@Valid @RequestBody ConfirmOtpRequest request) {
        return ResponseEntity.ok(authService.confirmEmailVerification(request));
    }

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ApiResponse<String>> requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
