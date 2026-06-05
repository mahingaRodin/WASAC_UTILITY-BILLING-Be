package com.wasac.utilitybilling.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {
    private int expirationMinutes;
    private int length;
}
