package com.wasac.utilitybilling.repository;

import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByRoleAndStatus(UserRole role, UserStatus status);
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);
}
