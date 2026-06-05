package com.java_ne_practical_tplt.services;

import com.java_ne_practical_tplt.payloads.dtos.UserDTO;
import com.java_ne_practical_tplt.payloads.requests.ConfirmOtpRequest;
import com.java_ne_practical_tplt.payloads.requests.EmailRequest;
import com.java_ne_practical_tplt.payloads.requests.ResetPasswordRequest;
import com.java_ne_practical_tplt.payloads.responses.ApiResponse;
import com.java_ne_practical_tplt.payloads.responses.AuthResponse;

public interface AuthService {
    AuthResponse login(String email, String password) throws Exception;
    AuthResponse signup(UserDTO req) throws Exception;
    ApiResponse requestEmailVerification(EmailRequest request);
    ApiResponse confirmEmailVerification(ConfirmOtpRequest request);
    ApiResponse requestPasswordReset(EmailRequest request);
    ApiResponse resetPassword(ResetPasswordRequest request);
}
