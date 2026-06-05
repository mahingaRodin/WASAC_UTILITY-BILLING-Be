package com.java_ne_practical_tplt.controllers;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and listing endpoints")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile by email header")
    public ResponseEntity<UserDTO> getUserProfile(@RequestHeader("X-User-Email") String email) throws Exception {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) throws Exception {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/all")
    @Operation(summary = "List all users")
    public ResponseEntity<List<UserDTO>> getAllUsers() throws Exception {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
