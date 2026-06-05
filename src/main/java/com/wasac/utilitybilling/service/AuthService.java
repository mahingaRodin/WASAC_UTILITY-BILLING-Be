package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.AuthResponse;
import com.wasac.utilitybilling.dto.ConfirmOtpRequest;
import com.wasac.utilitybilling.dto.EmailRequest;
import com.wasac.utilitybilling.dto.ResetPasswordRequest;
import com.wasac.utilitybilling.dto.UserDTO;

public interface AuthService {
    AuthResponse signup(UserDTO request);
    AuthResponse login(String email, String password);
    ApiResponse<String> requestEmailVerification(EmailRequest request);
    ApiResponse<String> confirmEmailVerification(ConfirmOtpRequest request);
    ApiResponse<String> requestPasswordReset(EmailRequest request);
    ApiResponse<String> resetPassword(ResetPasswordRequest request);
}
