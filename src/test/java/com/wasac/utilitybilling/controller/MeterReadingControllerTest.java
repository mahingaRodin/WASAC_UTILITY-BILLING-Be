package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.MeterReadingResponse;
import com.wasac.utilitybilling.service.MeterReadingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class MeterReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeterReadingService meterReadingService;

    private static final String VALID_READING = """
            {"meterId":"%s","previousReading":100,"currentReading":150,"readingDate":"2026-01-15"}
            """.formatted(UUID.randomUUID());

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOkForOperator() throws Exception {
        when(meterReadingService.create(any())).thenReturn(MeterReadingResponse.builder().build());
        mockMvc.perform(post("/api/meter-readings").contentType(MediaType.APPLICATION_JSON).content(VALID_READING))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void createForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/meter-readings").contentType(MediaType.APPLICATION_JSON).content(VALID_READING))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/meter-readings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void listOkForOperator() throws Exception {
        mockMvc.perform(get("/api/meter-readings")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listForbiddenForFinance() throws Exception {
        mockMvc.perform(get("/api/meter-readings")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void byMeterOkForAdmin() throws Exception {
        when(meterReadingService.getByMeter(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/meter-readings/meter/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void myReadingsOkForOperator() throws Exception {
        mockMvc.perform(get("/api/meter-readings/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myReadingsForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/meter-readings/me")).andExpect(status().isForbidden());
    }
}
