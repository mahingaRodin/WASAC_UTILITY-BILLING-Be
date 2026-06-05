package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.config.OtpProperties;
import com.wasac.utilitybilling.domain.OtpToken;
import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.OtpPurpose;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.repository.OtpTokenRepository;
import com.wasac.utilitybilling.service.MailService;
import com.wasac.utilitybilling.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final OtpProperties otpProperties;
    private final MailService mailService;

    @Override
    @Transactional
    public void generateAndSendOtp(User user, OtpPurpose purpose) {
        String otp = generateOtp(otpProperties.getLength());
        OtpToken token = OtpToken.builder()
                .user(user)
                .otpCode(otp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(otpProperties.getExpirationMinutes()))
                .used(false)
                .build();
        otpTokenRepository.save(token);

        String template = purpose == OtpPurpose.EMAIL_VERIFICATION ? "email-verification-otp" : "password-reset-otp";
        mailService.sendTemplateEmail(user.getEmail(), template, Map.of(
                "userName", user.getFullName(),
                "otp", otp,
                "expiryMinutes", otpProperties.getExpirationMinutes(),
                "appName", "WASAC Utility Billing"
        ));
    }

    @Override
    @Transactional
    public void validateOtp(User user, String otp, OtpPurpose purpose) {
        OtpToken token = otpTokenRepository.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, purpose)
                .orElseThrow(() -> new BadRequestException("OTP not found"));

        if (token.isUsed()) {
            throw new BadRequestException("OTP already used");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }
        if (!token.getOtpCode().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }
        token.setUsed(true);
        otpTokenRepository.save(token);
    }

    private String generateOtp(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
