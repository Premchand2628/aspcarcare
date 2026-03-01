package com.servicewasher.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.servicewasher.backend.user.*;
import java.util.Optional;

public interface WasherUserRepository extends JpaRepository<WasherUser, Long> {
  Optional<WasherUser> findByEmail(String email);
  boolean existsByEmail(String email);
}