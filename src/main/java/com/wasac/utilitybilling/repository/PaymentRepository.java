package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
