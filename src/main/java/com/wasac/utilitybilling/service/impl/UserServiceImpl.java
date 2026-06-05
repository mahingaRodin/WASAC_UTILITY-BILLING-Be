package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import com.wasac.utilitybilling.dto.AdminCreateUserRequest;
import com.wasac.utilitybilling.dto.UpdateUserRoleRequest;
import com.wasac.utilitybilling.dto.UpdateUserStatusRequest;
import com.wasac.utilitybilling.dto.UserResponse;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.UserMapper;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.service.MailService;
import com.wasac.utilitybilling.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MailService mailService;

    @Override
    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists.");
        }

        User saved = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .role(request.getRole())
                .emailVerified(false)
                .mustChangePassword(true)
                .build());

        sendActivationInvite(saved, request.getPassword());
        return userMapper.toResponse(saved);
    }

    private void sendActivationInvite(User user, String temporaryPassword) {
        String message = "An administrator created a " + user.getRole().name().replace("ROLE_", "")
                + " account for you on WASAC Utility Billing.\n\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "For security, you must set your own password before you can log in. "
                + "Call POST /api/auth/activate-account with your email, this temporary password, and your new password.";
        try {
            mailService.sendTemplateEmail(user.getEmail(), "account-notification", Map.of(
                    "title", "Activate Your WASAC Account",
                    "userName", user.getFullName(),
                    "message", message
            ));
        } catch (Exception ex) {
            log.error("Best-effort activation invite email failed for {}", user.getEmail(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID id, UpdateUserStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getRole() == UserRole.ROLE_ADMIN
                && request.getStatus() == UserStatus.INACTIVE
                && user.getStatus() == UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(UserRole.ROLE_ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new BadRequestException("Cannot deactivate the last active admin.");
        }
        user.setStatus(request.getStatus());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateRole(UUID id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        if (user.getRole() == UserRole.ROLE_ADMIN
                && request.getRole() != UserRole.ROLE_ADMIN
                && user.getStatus() == UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(UserRole.ROLE_ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new BadRequestException("Cannot change role of the last active admin.");
        }
        user.setRole(request.getRole());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new BadRequestException("You cannot delete your own account.");
        }
        if (user.getRole() == UserRole.ROLE_ADMIN
                && user.getStatus() == UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(UserRole.ROLE_ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new BadRequestException("Cannot delete the last active admin.");
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return userMapper.toResponse(user);
    }
}
