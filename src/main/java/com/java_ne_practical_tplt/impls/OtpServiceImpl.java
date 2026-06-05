package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.configs.OtpProperties;
import com.java_ne_practical_tplt.exceptions.BadRequestException;
import com.java_ne_practical_tplt.models.OtpToken;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;
import com.java_ne_practical_tplt.payloads.enums.EOtpPurpose;
import com.java_ne_practical_tplt.repositories.OtpTokenRepository;
import com.java_ne_practical_tplt.services.MailService;
import com.java_ne_practical_tplt.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final MailService mailService;
    private final OtpProperties otpProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSendOtp(User user, EOtpPurpose purpose) {
        String otpCode = generateOtpCode();
        LocalDateTime now = LocalDateTime.now();

        OtpToken otpToken = OtpToken.builder()
                .user(user)
                .otpCode(otpCode)
                .purpose(purpose)
                .expiresAt(now.plusMinutes(otpProperties.getExpirationMinutes()))
                .used(false)
                .createdAt(now)
                .build();

        otpTokenRepository.save(otpToken);
        sendOtpEmail(user, otpCode, purpose);
        return otpCode;
    }

    @Override
    @Transactional
    public void validateOtp(User user, String otpCode, EOtpPurpose purpose) {
        OtpToken otpToken = otpTokenRepository
                .findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, purpose)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP."));

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (!otpToken.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Invalid OTP code.");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int floor = bound / 10;
        int code = secureRandom.nextInt(bound - floor) + floor;
        return String.valueOf(code);
    }

    private void sendOtpEmail(User user, String otpCode, EOtpPurpose purpose) {
        Map<String, Object> variables = Map.of(
                "userName", user.getFirstName(),
                "otp", otpCode,
                "expiryMinutes", otpProperties.getExpirationMinutes(),
                "appName", "Template"
        );

        EEmailTemplateType templateType = switch (purpose) {
            case EMAIL_VERIFICATION -> EEmailTemplateType.EMAIL_VERIFICATION_OTP;
            case PASSWORD_RESET -> EEmailTemplateType.PASSWORD_RESET_OTP;
        };

        mailService.sendTemplateEmail(user.getEmail(), templateType, variables);
    }
}
