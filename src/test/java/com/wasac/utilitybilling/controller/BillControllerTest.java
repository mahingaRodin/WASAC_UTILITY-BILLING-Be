package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.service.BillService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillService billService;

    private static final String VALID_BILL = """
            {"billReference":"BILL-2026-01","customerId":"%s","meterId":"%s","billingYear":2026,
             "billingMonth":1,"unitsConsumed":12.5,"amountDue":5000,"dueDate":"2026-02-15","status":"UNPAID"}
            """.formatted(UUID.randomUUID(), UUID.randomUUID());

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOkForOperator() throws Exception {
        when(billService.create(any())).thenReturn(BillResponse.builder().build());
        mockMvc.perform(post("/api/bills").contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void createForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/bills").contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void pendingOkForFinance() throws Exception {
        mockMvc.perform(get("/api/bills/pending")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void pendingForbiddenForOperator() throws Exception {
        mockMvc.perform(get("/api/bills/pending")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void approveOkForFinance() throws Exception {
        when(billService.approve(any())).thenReturn(BillResponse.builder().build());
        mockMvc.perform(post("/api/bills/" + UUID.randomUUID() + "/approve")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void rejectOkForFinance() throws Exception {
        when(billService.reject(any())).thenReturn(BillResponse.builder().build());
        mockMvc.perform(post("/api/bills/" + UUID.randomUUID() + "/reject")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void approveForbiddenForCustomer() throws Exception {
        mockMvc.perform(post("/api/bills/" + UUID.randomUUID() + "/approve")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createRejectsInvalidMonth() throws Exception {
        String body = VALID_BILL.replace("\"billingMonth\":1", "\"billingMonth\":13");
        mockMvc.perform(post("/api/bills").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listOkForFinance() throws Exception {
        mockMvc.perform(get("/api/bills")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/bills")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void getByReferenceOkForFinance() throws Exception {
        when(billService.getByReference(anyString())).thenReturn(BillResponse.builder().build());
        mockMvc.perform(get("/api/bills/reference/BILL-2026-01")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void byCustomerOkForAdmin() throws Exception {
        mockMvc.perform(get("/api/bills/customer/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myBillsOkForCustomer() throws Exception {
        mockMvc.perform(get("/api/bills/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void myBillsForbiddenForFinance() throws Exception {
        mockMvc.perform(get("/api/bills/me")).andExpect(status().isForbidden());
    }
}
