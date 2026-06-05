package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.dto.ActivateAccountRequest;
import com.wasac.utilitybilling.dto.UserDTO;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.UserRepository userRepository;
    @Mock
    private com.wasac.utilitybilling.security.JwtProvider jwtProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private OtpService otpService;
    @Mock
    private MailService mailService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signupShouldRejectExistingEmail() {
        UserDTO dto = new UserDTO();
        dto.setEmail("exists@wasac.rw");
        dto.setFullName("John Doe");
        dto.setPhone("+2507");
        dto.setPassword("Password@1");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.signup(dto));
    }

    @Test
    void loginShouldBlockUntilPasswordIsChanged() {
        User user = User.builder()
                .email("operator@wasac.rw")
                .mustChangePassword(true)
                .emailVerified(false)
                .build();
        when(userRepository.findByEmail("operator@wasac.rw")).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> authService.login("operator@wasac.rw", "whatever"));
    }

    @Test
    void activateAccountShouldRejectWrongTemporaryPassword() {
        User user = User.builder()
                .email("operator@wasac.rw")
                .password("ENC")
                .mustChangePassword(true)
                .build();
        when(userRepository.findByEmail("operator@wasac.rw")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setEmail("operator@wasac.rw");
        request.setTemporaryPassword("wrong");
        request.setNewPassword("BrandNew@123");
        assertThrows(BadRequestException.class, () -> authService.activateAccount(request));
    }
}
