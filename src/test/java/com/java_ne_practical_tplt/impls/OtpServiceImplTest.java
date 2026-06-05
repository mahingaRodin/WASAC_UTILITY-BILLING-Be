package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.configs.OtpProperties;
import com.java_ne_practical_tplt.exceptions.BadRequestException;
import com.java_ne_practical_tplt.models.OtpToken;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EOtpPurpose;
import com.java_ne_practical_tplt.repositories.OtpTokenRepository;
import com.java_ne_practical_tplt.services.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private MailService mailService;

    @Mock
    private OtpProperties otpProperties;

    @InjectMocks
    private OtpServiceImpl otpService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@template.local")
                .firstName("Test")
                .build();
    }

    @Test
    void generateAndSendOtpPersistsTokenAndSendsEmail() {
        when(otpProperties.getExpirationMinutes()).thenReturn(10);
        when(otpProperties.getLength()).thenReturn(6);
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String otp = otpService.generateAndSendOtp(user, EOtpPurpose.EMAIL_VERIFICATION);

        assertThat(otp).hasSize(6);

        ArgumentCaptor<OtpToken> tokenCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getPurpose()).isEqualTo(EOtpPurpose.EMAIL_VERIFICATION);
        verify(mailService).sendTemplateEmail(eq(user.getEmail()), any(), any());
    }

    @Test
    void validateOtpMarksTokenAsUsed() {
        OtpToken token = OtpToken.builder()
                .otpCode("123456")
                .purpose(EOtpPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpTokenRepository.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, EOtpPurpose.PASSWORD_RESET))
                .thenReturn(Optional.of(token));
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        otpService.validateOtp(user, "123456", EOtpPurpose.PASSWORD_RESET);

        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void validateOtpRejectsExpiredCode() {
        OtpToken token = OtpToken.builder()
                .otpCode("123456")
                .purpose(EOtpPurpose.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(otpTokenRepository.findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(user, EOtpPurpose.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(token));

        assertThatThrownBy(() -> otpService.validateOtp(user, "123456", EOtpPurpose.EMAIL_VERIFICATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }
}
