package com.java_ne_practical_tplt.repositories;

import com.java_ne_practical_tplt.models.OtpToken;
import com.java_ne_practical_tplt.models.User;
import com.java_ne_practical_tplt.payloads.enums.EOtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            User user,
            EOtpPurpose purpose
    );
}
