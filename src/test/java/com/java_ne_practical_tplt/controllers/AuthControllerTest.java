package com.java_ne_practical_tplt.controllers;

import com.java_ne_practical_tplt.payloads.requests.ConfirmOtpRequest;
import com.java_ne_practical_tplt.payloads.requests.EmailRequest;
import com.java_ne_practical_tplt.payloads.responses.ApiResponse;
import com.java_ne_practical_tplt.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void requestEmailVerificationReturnsSuccess() throws Exception {
        when(authService.requestEmailVerification(any(EmailRequest.class)))
                .thenReturn(ApiResponse.builder().success(true).message("Verification code sent to your email.").build());

        mockMvc.perform(post("/api/auth/verify-email/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@template.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification code sent to your email."));
    }

    @Test
    void confirmEmailVerificationReturnsSuccess() throws Exception {
        when(authService.confirmEmailVerification(any(ConfirmOtpRequest.class)))
                .thenReturn(ApiResponse.builder().success(true).message("Email verified successfully.").build());

        mockMvc.perform(post("/api/auth/verify-email/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@template.local\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signupValidatesRequiredFields() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
