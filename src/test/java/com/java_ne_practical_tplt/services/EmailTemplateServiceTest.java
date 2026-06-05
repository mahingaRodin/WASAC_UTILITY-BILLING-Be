package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmailTemplateServiceTest {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Test
    void rendersEmailVerificationTemplate() {
        String html = emailTemplateService.render(
                EEmailTemplateType.EMAIL_VERIFICATION_OTP,
                Map.of("userName", "Rodin", "otp", "482910", "expiryMinutes", 10, "appName", "Template")
        );

        assertThat(html).contains("Verify your email");
        assertThat(html).contains("Rodin");
        assertThat(html).contains("482910");
    }

    @Test
    void rendersPasswordResetTemplate() {
        String html = emailTemplateService.render(
                EEmailTemplateType.PASSWORD_RESET_OTP,
                Map.of("userName", "Rodin", "otp", "193847", "expiryMinutes", 10, "appName", "Template")
        );

        assertThat(html).contains("Reset your password");
        assertThat(html).contains("193847");
    }

    @Test
    void rendersWelcomeTemplate() {
        String html = emailTemplateService.render(
                EEmailTemplateType.WELCOME,
                Map.of("userName", "Rodin", "appName", "Template")
        );

        assertThat(html).contains("Welcome to");
        assertThat(html).contains("Rodin");
    }

    @Test
    void rendersAccountNotificationTemplate() {
        String html = emailTemplateService.render(
                EEmailTemplateType.ACCOUNT_NOTIFICATION,
                Map.of(
                        "userName", "Rodin",
                        "title", "Security alert",
                        "message", "Your password was changed."
                )
        );

        assertThat(html).contains("Security alert");
        assertThat(html).contains("Your password was changed.");
    }
}
