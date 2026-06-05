package com.java_ne_practical_tplt.impls;

import com.java_ne_practical_tplt.configs.JwtProvider;
import com.java_ne_practical_tplt.exceptions.BadRequestException;
import com.java_ne_practical_tplt.mappers.UserMapper;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.enums.EEmailTemplateType;
import com.java_ne_practical_tplt.payloads.enums.EOtpPurpose;
import com.java_ne_practical_tplt.payloads.requests.ConfirmOtpRequest;
import com.java_ne_practical_tplt.payloads.requests.EmailRequest;
import com.java_ne_practical_tplt.payloads.requests.ResetPasswordRequest;
import com.java_ne_practical_tplt.payloads.responses.ApiResponse;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;
import com.java_ne_practical_tplt.repositories.UserRepository;
import com.java_ne_practical_tplt.services.AuthService;
import com.java_ne_practical_tplt.services.MailService;
import com.java_ne_practical_tplt.services.OtpService;
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
    private final JwtProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final OtpService otpService;
    private final MailService mailService;

    @Override
    public AuthResponse login(String email, String password) throws Exception {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("Invalid credentials.");
        }
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in.");
        }

        Authentication authentication = authenticate(email, password);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String token = tokenProvider.generateToken(authentication, user.getId());

        return buildAuthResponse(user, token, "Welcome " + user.getFirstName() + "!", "You have successfully logged in!");
    }

    @Override
    @Transactional
    public AuthResponse signup(UserDTO req) throws Exception {
        User existingUser = userRepository.findByEmail(req.getEmail());
        if (existingUser != null) {
            throw new BadRequestException("User already exists.");
        }

        LocalDateTime now = LocalDateTime.now();
        User newUser = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(req.getRole())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .emailVerified(false)
                .lastLogin(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(newUser);
        otpService.generateAndSendOtp(savedUser, EOtpPurpose.EMAIL_VERIFICATION);

        return buildAuthResponse(
                savedUser,
                null,
                "Account created",
                "A verification code has been sent to your email."
        );
    }

    @Override
    @Transactional
    public ApiResponse requestEmailVerification(EmailRequest request) {
        User user = requireExistingUser(request.getEmail());
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }
        otpService.generateAndSendOtp(user, EOtpPurpose.EMAIL_VERIFICATION);
        return ApiResponse.builder()
                .success(true)
                .message("Verification code sent to your email.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse confirmEmailVerification(ConfirmOtpRequest request) {
        User user = requireExistingUser(request.getEmail());
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }

        otpService.validateOtp(user, request.getOtp(), EOtpPurpose.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        mailService.sendTemplateEmail(
                user.getEmail(),
                EEmailTemplateType.WELCOME,
                Map.of("userName", user.getFirstName(), "appName", "Template")
        );

        return ApiResponse.builder()
                .success(true)
                .message("Email verified successfully.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse requestPasswordReset(EmailRequest request) {
        User user = requireExistingUser(request.getEmail());
        otpService.generateAndSendOtp(user, EOtpPurpose.PASSWORD_RESET);
        return ApiResponse.builder()
                .success(true)
                .message("Password reset code sent to your email.")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        User user = requireExistingUser(request.getEmail());
        otpService.validateOtp(user, request.getOtp(), EOtpPurpose.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        mailService.sendTemplateEmail(
                user.getEmail(),
                EEmailTemplateType.ACCOUNT_NOTIFICATION,
                Map.of(
                        "userName", user.getFirstName(),
                        "title", "Password updated",
                        "message", "Your password was changed successfully. If this wasn't you, contact support immediately."
                )
        );

        return ApiResponse.builder()
                .success(true)
                .message("Password reset successfully.")
                .build();
    }

    private User requireExistingUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new BadRequestException("No account found with this email.");
        }
        return user;
    }

    private AuthResponse buildAuthResponse(User user, String token, String title, String message) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setUser(UserMapper.toDTO(user));
        authResponse.setTitle(title);
        authResponse.setMessage(message);
        return authResponse;
    }

    private Authentication authenticate(String email, String password) throws Exception {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadRequestException("Invalid credentials.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
