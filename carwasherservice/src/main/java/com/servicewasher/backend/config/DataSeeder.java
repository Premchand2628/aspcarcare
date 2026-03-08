package com.servicewasher.backend.config;

import com.servicewasher.backend.user.UserRole;
import com.servicewasher.backend.user.WasherUser;
import com.servicewasher.backend.repository.WasherUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final WasherUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    String email = "washer1@asp.com";
    if (!userRepository.existsByEmail(email)) {
      WasherUser user = new WasherUser();
      user.setName("Washer One");
      user.setEmail(email);
      user.setPassword(passwordEncoder.encode("Washer@123"));
      user.setRole(UserRole.WASHER);
      user.setEnabled(true);
      userRepository.save(user);
    }
  }
}