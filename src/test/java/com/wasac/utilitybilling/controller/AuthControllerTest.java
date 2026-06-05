package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.AuthResponse;
import com.wasac.utilitybilling.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void signupShouldBePublicAndReturnOkForValidBody() throws Exception {
        when(authService.signup(any())).thenReturn(AuthResponse.builder().build());
        String body = """
                {"fullName":"John Doe","email":"john@wasac.rw","phone":"0788000000","password":"password123","role":"ROLE_CUSTOMER"}
                """;
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void signupShouldRejectInvalidBody() throws Exception {
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signupShouldRejectInvalidEmailFormat() throws Exception {
        String body = """
                {"fullName":"John","email":"not-an-email","phone":"0788","password":"password123","role":"ROLE_CUSTOMER"}
                """;
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldBePublicAndReturnOkForValidBody() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn(AuthResponse.builder().build());
        String body = """
                {"email":"john@wasac.rw","password":"password123"}
                """;
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldRejectMissingPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@wasac.rw\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void activateAccountShouldBePublicAndReturnOkForValidBody() throws Exception {
        when(authService.activateAccount(any())).thenReturn(ApiResponse.<String>builder().success(true).build());
        String body = """
                {"email":"operator@wasac.rw","temporaryPassword":"Temp@123","newPassword":"MyNewPass@1"}
                """;
        mockMvc.perform(post("/api/auth/activate-account").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    @Test
    void activateAccountShouldRejectShortNewPassword() throws Exception {
        String body = """
                {"email":"operator@wasac.rw","temporaryPassword":"Temp@123","newPassword":"short"}
                """;
        mockMvc.perform(post("/api/auth/activate-account").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
