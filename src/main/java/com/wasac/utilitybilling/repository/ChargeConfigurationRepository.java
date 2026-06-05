package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChargeConfigurationRepository extends JpaRepository<ChargeConfiguration, UUID> {
}
