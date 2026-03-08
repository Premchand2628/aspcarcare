package com.carwash.otplogin.repository;

import com.carwash.otplogin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    // Find user by phone
    Optional<User> findByPhone(String phone);
    
    // Find user by email
    Optional<User> findByEmail(String email);
}
