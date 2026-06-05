package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TariffTierRepository extends JpaRepository<TariffTier, UUID> {
    List<TariffTier> findByTariffConfiguration_Id(UUID tariffConfigurationId);
}
