package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.dto.UserDTO;
import com.wasac.utilitybilling.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signupShouldRejectExistingEmail() {
        UserDTO dto = new UserDTO();
        dto.setEmail("exists@wasac.rw");
        dto.setFullName("John Doe");
        dto.setPhone("+2507");
        dto.setPassword("Password@1");
        dto.setRole(UserRole.ROLE_CUSTOMER);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.signup(dto));
    }
}
