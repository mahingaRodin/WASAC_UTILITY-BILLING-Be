package com.wasac.utilitybilling.seed;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import com.wasac.utilitybilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${app.seed.admin.phone:+250700000000}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        if (!seedEnabled || userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.save(User.builder()
                .fullName(adminFullName)
                .email(adminEmail)
                .phone(adminPhone)
                .password(passwordEncoder.encode(adminPassword))
                .status(UserStatus.ACTIVE)
                .role(UserRole.ROLE_ADMIN)
                .emailVerified(true)
                .build());
    }
}
