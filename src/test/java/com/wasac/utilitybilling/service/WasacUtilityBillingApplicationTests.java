package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.WasacUtilityBillingApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = WasacUtilityBillingApplication.class,
        properties = "spring.flyway.enabled=false"
)
class WasacUtilityBillingApplicationTests {
    @Test
    void contextLoads() {
    }
}
