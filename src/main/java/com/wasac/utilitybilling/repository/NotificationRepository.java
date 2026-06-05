package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Notification> findByCustomer_IdOrderByCreatedAtDesc(UUID customerId);
    Page<Notification> findByCustomer_EmailOrderByCreatedAtDesc(String email, Pageable pageable);
}
