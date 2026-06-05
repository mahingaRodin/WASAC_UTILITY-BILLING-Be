package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.UserResponse;
import com.wasac.utilitybilling.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.flyway.enabled=false",
                "app.seed.enabled=false"
        }
)
@AutoConfigureMockMvc
class UserControllerRbacTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldReturnForbiddenWhenNonAdminAccessesUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnOkWhenAdminAccessesUsers() throws Exception {
        when(userService.list(org.springframework.data.domain.PageRequest.of(0, 20)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/api/users").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }
}
