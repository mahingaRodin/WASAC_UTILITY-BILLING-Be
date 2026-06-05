package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.domain.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<Customer> findByEmail(String email);
    Page<Customer> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Customer> findByStatus(CustomerStatus status);
}
