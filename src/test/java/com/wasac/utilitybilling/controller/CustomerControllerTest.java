package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.CustomerResponse;
import com.wasac.utilitybilling.service.CustomerService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private static final String VALID_CUSTOMER = """
            {"nationalId":"1199900001","fullName":"Alice","email":"alice@wasac.rw","phone":"0788000000",
             "status":"ACTIVE","address":{"province":"Kigali","district":"Gasabo","sector":"Remera","cell":"Nyabisindu","village":"Amahoro"}}
            """;

    private static final String VALID_UPDATE = """
            {"fullName":"Alice B","email":"alice@wasac.rw","phone":"0788111111",
             "address":{"province":"Kigali","district":"Gasabo","sector":"Remera","cell":"Nyabisindu","village":"Amahoro"}}
            """;

    @Test
    @WithMockUser(roles = "FINANCE")
    void createForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(VALID_CUSTOMER))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOkForOperator() throws Exception {
        when(customerService.create(any())).thenReturn(CustomerResponse.builder().build());
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(VALID_CUSTOMER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRejectsMissingAddress() throws Exception {
        String body = """
                {"nationalId":"119","fullName":"Bob","email":"bob@wasac.rw","phone":"078","status":"ACTIVE"}
                """;
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listOkForFinance() throws Exception {
        mockMvc.perform(get("/api/customers")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/customers")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void getByIdOkForOperator() throws Exception {
        when(customerService.getById(any())).thenReturn(CustomerResponse.builder().build());
        mockMvc.perform(get("/api/customers/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateOkForOperator() throws Exception {
        when(customerService.update(any(), any())).thenReturn(CustomerResponse.builder().build());
        mockMvc.perform(put("/api/customers/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_UPDATE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateStatusForbiddenForOperator() throws Exception {
        mockMvc.perform(patch("/api/customers/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatusOkForAdmin() throws Exception {
        when(customerService.updateStatus(any(), any())).thenReturn(CustomerResponse.builder().build());
        mockMvc.perform(patch("/api/customers/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deleteForbiddenForOperator() throws Exception {
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOkForAdmin() throws Exception {
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void myProfileOkForCustomer() throws Exception {
        when(customerService.getMyProfile()).thenReturn(CustomerResponse.builder().build());
        mockMvc.perform(get("/api/customers/me")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void myProfileForbiddenForAdmin() throws Exception {
        mockMvc.perform(get("/api/customers/me")).andExpect(status().isForbidden());
    }
}
