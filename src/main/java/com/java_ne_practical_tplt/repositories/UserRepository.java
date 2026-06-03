package com.java_ne_practical_tplt.repositories;

import com.java_ne_practical_tplt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
}
