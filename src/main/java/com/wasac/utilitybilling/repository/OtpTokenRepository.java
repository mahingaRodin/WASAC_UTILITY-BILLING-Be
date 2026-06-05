package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.OtpToken;
import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(User user, OtpPurpose purpose);
}
