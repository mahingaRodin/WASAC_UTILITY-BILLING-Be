package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.MeterResponse;
import com.wasac.utilitybilling.service.MeterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
class MeterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeterService meterService;

    private static final String VALID_METER = """
            {"meterNumber":"MTR-001","type":"WATER","installationDate":"2020-01-01","status":"ACTIVE","customerId":"%s"}
            """.formatted(UUID.randomUUID());

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOkForOperator() throws Exception {
        when(meterService.create(any())).thenReturn(MeterResponse.builder().build());
        mockMvc.perform(post("/api/meters").contentType(MediaType.APPLICATION_JSON).content(VALID_METER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void createForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/meters").contentType(MediaType.APPLICATION_JSON).content(VALID_METER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/meters").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listOkForFinance() throws Exception {
        mockMvc.perform(get("/api/meters")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/meters")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void byCustomerOkForOperator() throws Exception {
        mockMvc.perform(get("/api/meters/customer/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateStatusOkForOperator() throws Exception {
        when(meterService.updateStatus(any(), any())).thenReturn(MeterResponse.builder().build());
        mockMvc.perform(patch("/api/meters/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deleteForbiddenForOperator() throws Exception {
        mockMvc.perform(delete("/api/meters/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOkForAdmin() throws Exception {
        mockMvc.perform(delete("/api/meters/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myMetersOkForCustomer() throws Exception {
        when(meterService.getMyMeters()).thenReturn(List.of());
        mockMvc.perform(get("/api/meters/me")).andExpect(status().isOk());
    }

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/meters")).andExpect(status().is4xxClientError());
    }
}
