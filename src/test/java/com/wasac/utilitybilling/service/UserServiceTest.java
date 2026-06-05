package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import com.wasac.utilitybilling.dto.AdminCreateUserRequest;
import com.wasac.utilitybilling.dto.UpdateUserStatusRequest;
import com.wasac.utilitybilling.dto.UserResponse;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.mapper.UserMapper;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private MailService mailService;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createShouldRejectDuplicateEmail() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("exists@wasac.rw");
        when(userRepository.existsByEmail("exists@wasac.rw")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userService.create(request));
    }

    @Test
    void createShouldFlagMustChangePasswordAndSendActivationInvite() {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setEmail("operator@wasac.rw");
        request.setFullName("Olga Muhorakeye");
        request.setPhone("0788000000");
        request.setPassword("Temp@1234");
        request.setRole(UserRole.ROLE_OPERATOR);

        when(userRepository.existsByEmail("operator@wasac.rw")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(UserResponse.builder().build());

        userService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertTrue(saved.isMustChangePassword(), "new staff must be forced to change password");
        assertTrue(!saved.isEmailVerified(), "new staff should not be verified until activation");

        verify(mailService, times(1)).sendTemplateEmail(eq("operator@wasac.rw"), eq("account-notification"), any());
    }

    @Test
    void updateStatusShouldRejectDeactivatingLastAdmin() {
        UUID id = UUID.randomUUID();
        User admin = User.builder().id(id).role(UserRole.ROLE_ADMIN).status(UserStatus.ACTIVE).build();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(id)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndStatus(UserRole.ROLE_ADMIN, UserStatus.ACTIVE)).thenReturn(1L);
        assertThrows(BadRequestException.class, () -> userService.updateStatus(id, request));
    }
}
