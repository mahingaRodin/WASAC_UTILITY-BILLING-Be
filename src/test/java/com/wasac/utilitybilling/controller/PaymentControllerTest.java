package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.service.PaymentService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private static final String VALID_PAYMENT = """
            {"billReference":"BILL-2026-01","amountPaid":2500,"paymentMethod":"MOBILE_MONEY","paymentDate":"2026-02-10"}
            """;

    @Test
    @WithMockUser(roles = "FINANCE")
    void processOkForFinance() throws Exception {
        when(paymentService.process(any())).thenReturn(PaymentResponse.builder().build());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(VALID_PAYMENT))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void processOkForCustomer() throws Exception {
        when(paymentService.process(any())).thenReturn(PaymentResponse.builder().build());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(VALID_PAYMENT))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void processForbiddenForOperator() throws Exception {
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(VALID_PAYMENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void pendingOkForFinance() throws Exception {
        mockMvc.perform(get("/api/payments/pending")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void approveOkForFinance() throws Exception {
        when(paymentService.approve(any())).thenReturn(PaymentResponse.builder().build());
        mockMvc.perform(post("/api/payments/" + UUID.randomUUID() + "/approve")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void rejectOkForFinance() throws Exception {
        when(paymentService.reject(any())).thenReturn(PaymentResponse.builder().build());
        mockMvc.perform(post("/api/payments/" + UUID.randomUUID() + "/reject")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void approveForbiddenForCustomer() throws Exception {
        mockMvc.perform(post("/api/payments/" + UUID.randomUUID() + "/approve")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void processRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/payments")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void listForbiddenForOperator() throws Exception {
        mockMvc.perform(get("/api/payments")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void byBillReferenceOkForFinance() throws Exception {
        when(paymentService.getByBillReference(anyString())).thenReturn(List.of());
        mockMvc.perform(get("/api/payments/bill/BILL-2026-01")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getByIdOkForFinance() throws Exception {
        when(paymentService.getById(any())).thenReturn(PaymentResponse.builder().build());
        mockMvc.perform(get("/api/payments/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myPaymentsOkForCustomer() throws Exception {
        mockMvc.perform(get("/api/payments/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void myPaymentsForbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/api/payments/me")).andExpect(status().isForbidden());
    }
}
