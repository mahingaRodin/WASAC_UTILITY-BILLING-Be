package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.UserResponse;
import com.wasac.utilitybilling.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private static final String VALID_USER = """
            {"fullName":"Jane Doe","email":"jane@wasac.rw","phone":"0788000000","password":"password123","role":"ROLE_OPERATOR"}
            """;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void listForbiddenForOperator() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserOkForAdmin() throws Exception {
        when(userService.create(any())).thenReturn(UserResponse.builder().build());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_USER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void createUserForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_USER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusOkForAdmin() throws Exception {
        when(userService.updateStatus(any(), any())).thenReturn(UserResponse.builder().build());
        mockMvc.perform(patch("/api/users/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoleOkForAdmin() throws Exception {
        when(userService.updateRole(any(), any())).thenReturn(UserResponse.builder().build());
        mockMvc.perform(patch("/api/users/" + UUID.randomUUID() + "/role")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ROLE_FINANCE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOkForAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void meAccessibleToAnyAuthenticatedUser() throws Exception {
        when(userService.me()).thenReturn(UserResponse.builder().build());
        mockMvc.perform(get("/api/users/me")).andExpect(status().isOk());
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().is4xxClientError());
    }
}
