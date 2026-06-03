package com.java_ne_practical_tplt.controllers;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(
            @RequestHeader("X-User-Email") String email
    ) throws Exception {
        UserDTO user = userService.findByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(
            @PathVariable UUID userId
    ) throws Exception {
        UserDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() throws Exception {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
