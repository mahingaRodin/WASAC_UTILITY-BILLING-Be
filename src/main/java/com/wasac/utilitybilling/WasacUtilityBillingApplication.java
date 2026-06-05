package com.wasac.utilitybilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WasacUtilityBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasacUtilityBillingApplication.class, args);
    }
}
