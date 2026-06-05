package com.java_ne_practical_tplt.controllers;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.requests.ConfirmOtpRequest;
import com.java_ne_practical_tplt.payloads.requests.EmailRequest;
import com.java_ne_practical_tplt.payloads.requests.LoginRequest;
import com.java_ne_practical_tplt.payloads.requests.ResetPasswordRequest;
import com.java_ne_practical_tplt.payloads.responses.ApiResponse;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;
import com.java_ne_practical_tplt.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Signup, login, email verification, and password reset")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates an account and sends an email verification OTP.")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserDTO userDTO) throws Exception {
        return ResponseEntity.ok(authService.signup(userDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a verified user and returns a JWT token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) throws Exception {
        return ResponseEntity.ok(authService.login(req.getEmail(), req.getPassword()));
    }

    @PostMapping("/verify-email/request")
    @Operation(summary = "Request email verification OTP", description = "Resends a verification code to the user's email.")
    public ResponseEntity<ApiResponse> requestEmailVerification(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.requestEmailVerification(request));
    }

    @PostMapping("/verify-email/confirm")
    @Operation(summary = "Confirm email verification OTP", description = "Verifies the user's email using the OTP code.")
    public ResponseEntity<ApiResponse> confirmEmailVerification(@Valid @RequestBody ConfirmOtpRequest request) {
        return ResponseEntity.ok(authService.confirmEmailVerification(request));
    }

    @PostMapping("/forgot-password/request")
    @Operation(summary = "Request password reset OTP", description = "Sends a password reset code to the user's email.")
    public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset password with OTP", description = "Resets the user's password after OTP validation.")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
