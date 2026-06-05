package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.WasacUtilityBillingApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = WasacUtilityBillingApplication.class,
        properties = "spring.flyway.enabled=false"
)
class EmailTemplateRenderTest {

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Test
    void rendersAccountNotificationWithMultilineMessage() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("title", "Payment Completed");
        vars.put("userName", "Alice Uwase");
        vars.put("message", "Dear Alice Uwase,\nYour 1/2026 utility bill of 5000 FRW has been successfully processed.");

        String html = emailTemplateService.render("account-notification", vars);

        assertThat(html).contains("Payment Completed");
        assertThat(html).contains("Alice Uwase");
        assertThat(html).contains("successfully processed");
        assertThat(html).contains("WASAC");
    }

    @Test
    void rendersEmailVerificationOtp() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "Bob Habimana");
        vars.put("otp", "482915");
        vars.put("expiryMinutes", 10);
        vars.put("appName", "WASAC Utility Billing");

        String html = emailTemplateService.render("email-verification-otp", vars);

        assertThat(html).contains("482915");
        assertThat(html).contains("Bob Habimana");
        assertThat(html).contains("Verify your email");
    }

    @Test
    void rendersPasswordResetOtp() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "Carol Ingabire");
        vars.put("otp", "771203");
        vars.put("expiryMinutes", 10);
        vars.put("appName", "WASAC Utility Billing");

        String html = emailTemplateService.render("password-reset-otp", vars);

        assertThat(html).contains("771203");
        assertThat(html).contains("Carol Ingabire");
        assertThat(html).contains("Reset your password");
    }

    @Test
    void rendersWelcome() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "David Niyonzima");
        vars.put("appName", "WASAC Utility Billing");

        String html = emailTemplateService.render("welcome", vars);

        assertThat(html).contains("David Niyonzima");
        assertThat(html).contains("Welcome to");
    }
}
