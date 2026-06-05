package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.AdminCreateUserRequest;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.UpdateUserRoleRequest;
import com.wasac.utilitybilling.dto.UpdateUserStatusRequest;
import com.wasac.utilitybilling.dto.UserResponse;
import com.wasac.utilitybilling.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Create user")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User created")
                .data(userService.create(request))
                .build());
    }

    @Operation(summary = "List users")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .success(true)
                .message("Users retrieved")
                .data(userService.list(pageable))
                .build());
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User retrieved")
                .data(userService.getById(id))
                .build());
    }

    @Operation(summary = "Update user status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(@PathVariable UUID id,
                                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User status updated")
                .data(userService.updateStatus(id, request))
                .build());
    }

    @Operation(summary = "Update user role")
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User role updated")
                .data(userService.updateRole(id, request))
                .build());
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("User deleted")
                .build());
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("User profile retrieved")
                .data(userService.me())
                .build());
    }
}
