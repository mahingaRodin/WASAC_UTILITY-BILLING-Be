package com.java_ne_practical_tplt.seedsInitializers;

import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EUserRole;
import com.java_ne_practical_tplt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SeedInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SYSTEM_ADMIN_EMAIL = "agressive.one04@gmail.com";
    private static final String SYSTEM_ADMIN_PASSWORD = "Rodin!132";

    @Override
    public void run(String... args) throws Exception {
        seedSystemAdmin();
    }

    private void seedSystemAdmin() {
        User existingAdmin = userRepository.findByEmail(SYSTEM_ADMIN_EMAIL);
        LocalDateTime now = LocalDateTime.now();

        if(existingAdmin == null) {
            User admin = User.builder()
                    .email(SYSTEM_ADMIN_EMAIL)
                    .password(passwordEncoder.encode(SYSTEM_ADMIN_PASSWORD))
                    .firstName("Aggressive")
                    .lastName("Admin")
                    .phone("+250794415318")
                    .role(EUserRole.ROLE_SYSTEM_ADMIN)
                    .createdAt(now)
                    .updatedAt(now)
                    .lastLogin(now)
                    .build();
            userRepository.save(admin);
        }
    }
}
