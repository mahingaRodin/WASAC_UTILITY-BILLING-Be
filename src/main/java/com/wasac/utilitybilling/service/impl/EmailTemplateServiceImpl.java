package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {
    private final TemplateEngine templateEngine;

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    public String subject(String templateName, Map<String, Object> variables) {
        return switch (templateName) {
            case "email-verification-otp" -> "WASAC Email Verification OTP";
            case "password-reset-otp" -> "WASAC Password Reset OTP";
            case "welcome" -> "Welcome to WASAC Utility Billing";
            case "account-notification" -> String.valueOf(variables.getOrDefault("title", "WASAC Account Notification"));
            default -> "WASAC Utility Billing Notification";
        };
    }
}
