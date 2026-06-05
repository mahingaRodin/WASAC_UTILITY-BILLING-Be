package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ChargeResponse;
import com.wasac.utilitybilling.dto.TariffResponse;
import com.wasac.utilitybilling.service.TariffService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"spring.flyway.enabled=false", "app.seed.enabled=false"})
@AutoConfigureMockMvc
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TariffService tariffService;

    private static final String VALID_TARIFF = """
            {"utilityType":"WATER","tariffType":"FLAT","flatRate":350,"effectiveFrom":"2030-01-01","version":1}
            """;

    private static final String VALID_CHARGE = """
            {"chargeType":"VAT","valueType":"PERCENTAGE","value":18,"effectiveFrom":"2030-01-01","version":1}
            """;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTariffOkForAdmin() throws Exception {
        when(tariffService.createTariff(any())).thenReturn(TariffResponse.builder().build());
        mockMvc.perform(post("/api/config/tariffs").contentType(MediaType.APPLICATION_JSON).content(VALID_TARIFF))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createTariffForbiddenForOperator() throws Exception {
        mockMvc.perform(post("/api/config/tariffs").contentType(MediaType.APPLICATION_JSON).content(VALID_TARIFF))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTariffRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/config/tariffs").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void listTariffsOkForFinance() throws Exception {
        when(tariffService.listTariffs()).thenReturn(List.of());
        mockMvc.perform(get("/api/config/tariffs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listTariffsForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/config/tariffs")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateTariffOkForAdmin() throws Exception {
        when(tariffService.deactivateTariff(any())).thenReturn(TariffResponse.builder().build());
        mockMvc.perform(patch("/api/config/tariffs/" + UUID.randomUUID() + "/deactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createChargeOkForAdmin() throws Exception {
        when(tariffService.createCharge(any())).thenReturn(ChargeResponse.builder().build());
        mockMvc.perform(post("/api/config/charges").contentType(MediaType.APPLICATION_JSON).content(VALID_CHARGE))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FINANCE")
    void createChargeForbiddenForFinance() throws Exception {
        mockMvc.perform(post("/api/config/charges").contentType(MediaType.APPLICATION_JSON).content(VALID_CHARGE))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void listChargesOkForOperator() throws Exception {
        when(tariffService.listCharges()).thenReturn(List.of());
        mockMvc.perform(get("/api/config/charges")).andExpect(status().isOk());
    }
}
