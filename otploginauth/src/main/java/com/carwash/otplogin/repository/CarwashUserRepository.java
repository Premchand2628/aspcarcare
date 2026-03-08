package com.carwash.otplogin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.carwash.otplogin.entity.User;

import java.util.Optional;

@Repository
public interface CarwashUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
}