package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.TariffConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TariffConfigurationRepository extends JpaRepository<TariffConfiguration, UUID> {
}
