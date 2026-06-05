package com.java_ne_practical_tplt.payloads.enums;

public enum EEmailTemplateType {
    EMAIL_VERIFICATION_OTP("email-verification-otp", "Verify your email address"),
    PASSWORD_RESET_OTP("password-reset-otp", "Reset your password"),
    WELCOME("welcome", "Welcome aboard"),
    ACCOUNT_NOTIFICATION("account-notification", "Account notification");

    private final String templateName;
    private final String defaultSubject;

    EEmailTemplateType(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }
}
