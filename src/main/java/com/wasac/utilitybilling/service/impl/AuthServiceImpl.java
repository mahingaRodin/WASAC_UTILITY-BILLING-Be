package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.OtpPurpose;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.AuthResponse;
import com.wasac.utilitybilling.dto.ConfirmOtpRequest;
import com.wasac.utilitybilling.dto.EmailRequest;
import com.wasac.utilitybilling.dto.ResetPasswordRequest;
import com.wasac.utilitybilling.dto.UserDTO;
import com.wasac.utilitybilling.dto.UserView;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.security.JwtProvider;
import com.wasac.utilitybilling.service.AuthService;
import com.wasac.utilitybilling.service.CustomUserDetailsService;
import com.wasac.utilitybilling.service.MailService;
import com.wasac.utilitybilling.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final OtpService otpService;
    private final MailService mailService;

    @Override
    @Transactional
    public AuthResponse signup(UserDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists.");
        }
        User user = userRepository.save(User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .role(request.getRole())
                .emailVerified(false)
                .build());

        otpService.generateAndSendOtp(user, OtpPurpose.EMAIL_VERIFICATION);
        return AuthResponse.builder().token(null).user(toView(user)).build();
    }

    @Override
    @Transactional
    public AuthResponse login(String email, String password) {
        User user = getUser(email);
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email.");
        }
        Authentication authentication = authenticate(email, password);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String token = jwtProvider.generateToken(authentication, user.getId());
        return AuthResponse.builder().token(token).user(toView(user)).build();
    }

    @Override
    @Transactional
    public ApiResponse<String> requestEmailVerification(EmailRequest request) {
        User user = getUser(request.getEmail());
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }
        otpService.generateAndSendOtp(user, OtpPurpose.EMAIL_VERIFICATION);
        return ApiResponse.<String>builder().success(true).message("Verification OTP sent.").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> confirmEmailVerification(ConfirmOtpRequest request) {
        User user = getUser(request.getEmail());
        otpService.validateOtp(user, request.getOtp(), OtpPurpose.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
        userRepository.save(user);
        mailService.sendTemplateEmail(user.getEmail(), "welcome", Map.of(
                "userName", user.getFullName(),
                "appName", "WASAC Utility Billing"));
        return ApiResponse.<String>builder().success(true).message("Email verified successfully.").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> requestPasswordReset(EmailRequest request) {
        User user = getUser(request.getEmail());
        otpService.generateAndSendOtp(user, OtpPurpose.PASSWORD_RESET);
        return ApiResponse.<String>builder().success(true).message("Password reset OTP sent.").build();
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        User user = getUser(request.getEmail());
        otpService.validateOtp(user, request.getOtp(), OtpPurpose.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        mailService.sendTemplateEmail(user.getEmail(), "account-notification", Map.of(
                "userName", user.getFullName(),
                "title", "Password Updated",
                "message", "Your password has been updated successfully."
        ));
        return ApiResponse.<String>builder().success(true).message("Password reset successful.").build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User not found."));
    }

    private Authentication authenticate(String email, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadRequestException("Invalid credentials.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private UserView toView(User user) {
        return UserView.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
