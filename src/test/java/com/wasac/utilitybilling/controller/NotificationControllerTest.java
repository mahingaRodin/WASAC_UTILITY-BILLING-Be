package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void listForbiddenForOperator() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void byCustomerOkForFinance() throws Exception {
        when(notificationService.getByCustomerId(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/notifications/customer/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void byCustomerForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/notifications/customer/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myNotificationsOkForCustomer() throws Exception {
        mockMvc.perform(get("/api/notifications/me")).andExpect(status().isOk());
    }

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/notifications")).andExpect(status().is4xxClientError());
    }
}
