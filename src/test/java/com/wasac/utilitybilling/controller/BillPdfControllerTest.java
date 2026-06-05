package com.wasac.utilitybilling.controller;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class BillPdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillService billService;

    @Test
    @WithMockUser(roles = "FINANCE")
    void getBillPdfShouldReturnPdfForFinance() throws Exception {
        UUID id = UUID.randomUUID();
        when(billService.generateBillPdf(id)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/bills/{id}/pdf", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void getBillPdfShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/bills/{id}/pdf", UUID.randomUUID()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReceiptShouldReturnPdfForAdmin() throws Exception {
        UUID id = UUID.randomUUID();
        when(billService.generateReceiptPdf(id)).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/api/bills/{id}/receipt", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
