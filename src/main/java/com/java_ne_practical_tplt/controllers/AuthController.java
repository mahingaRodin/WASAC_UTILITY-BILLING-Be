package com.java_ne_practical_tplt.controllers;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.requests.LoginRequest;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;
import com.java_ne_practical_tplt.services.AuthService;
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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody UserDTO userDTO
    ) throws Exception {
        AuthResponse authResponse = authService.signup(userDTO);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req
    ) throws Exception {
        AuthResponse authResponse = authService.login(req.getEmail(),req.getPassword());
        return ResponseEntity.ok(authResponse);
    }
}
