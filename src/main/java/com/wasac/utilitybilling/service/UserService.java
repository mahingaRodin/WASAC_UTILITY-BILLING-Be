package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.AdminCreateUserRequest;
import com.wasac.utilitybilling.dto.UpdateUserRoleRequest;
import com.wasac.utilitybilling.dto.UpdateUserStatusRequest;
import com.wasac.utilitybilling.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse create(AdminCreateUserRequest request);
    Page<UserResponse> list(Pageable pageable);
    UserResponse getById(UUID id);
    UserResponse updateStatus(UUID id, UpdateUserStatusRequest request);
    UserResponse updateRole(UUID id, UpdateUserRoleRequest request);
    void delete(UUID id);
    UserResponse me();
}
