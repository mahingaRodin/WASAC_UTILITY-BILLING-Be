package com.java_ne_practical_tplt.seedsInitializers;

import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EUserRole;
import com.java_ne_practical_tplt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.first-name}")
    private String adminFirstName;

    @Value("${app.seed.admin.last-name}")
    private String adminLastName;

    @Value("${app.seed.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            return;
        }
        seedSystemAdmin();
    }

    private void seedSystemAdmin() {
        User existingAdmin = userRepository.findByEmail(adminEmail);
        LocalDateTime now = LocalDateTime.now();

        if (existingAdmin == null) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .phone(adminPhone)
                    .role(EUserRole.ROLE_SYSTEM_ADMIN)
                    .emailVerified(true)
                    .createdAt(now)
                    .updatedAt(now)
                    .lastLogin(now)
                    .build();
            userRepository.save(admin);
        }
    }
}
