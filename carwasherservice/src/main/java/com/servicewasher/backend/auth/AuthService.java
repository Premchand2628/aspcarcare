package com.servicewasher.backend.auth;

import com.servicewasher.backend.auth.dto.AuthResponse;
import com.servicewasher.backend.auth.dto.LoginRequest;
import com.servicewasher.backend.auth.dto.RegisterRequest;
import com.servicewasher.backend.auth.dto.ResetPasswordRequest;
import com.servicewasher.backend.auth.dto.ResetPasswordResponse;
import com.servicewasher.backend.security.JwtService;
import com.servicewasher.backend.user.UserRole;
import com.servicewasher.backend.user.WasherUser;
import com.servicewasher.backend.repository.WasherUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final WasherUserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw new RuntimeException("Email already registered");
    }

    WasherUser user = new WasherUser();
    user.setName(request.name());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(UserRole.WASHER);
    user.setEnabled(true);

    WasherUser saved = userRepository.save(user);
    String token = jwtService.generateToken(toUserDetails(saved));
    return toResponse(saved, token);
  }

  public AuthResponse login(LoginRequest request) {
    String email = request.email().trim().toLowerCase();

    authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(email, request.password())
    );

    WasherUser user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("Invalid credentials"));

    String token = jwtService.generateToken(toUserDetails(user));
    return toResponse(user, token);
  }

  public AuthResponse me(String email) {
    WasherUser user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));
    return toResponse(user, null);
  }

  public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
    String email = request.email().trim().toLowerCase();

    WasherUser user = userRepository.findByEmail(email)
      .orElseThrow(() -> new RuntimeException("User not found"));

    String plainPassword = (request.newPassword() != null && !request.newPassword().isBlank())
      ? request.newPassword().trim()
      : generateTempPassword();

    user.setPassword(passwordEncoder.encode(plainPassword));
    userRepository.save(user);

    return new ResetPasswordResponse(
      "Password reset successful",
      (request.newPassword() == null || request.newPassword().isBlank()) ? plainPassword : null
    );
  }

  private String generateTempPassword() {
    String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
    SecureRandom random = new SecureRandom();
    StringBuilder value = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      value.append(chars.charAt(random.nextInt(chars.length())));
    }
    return value.toString();
  }

  private UserDetails toUserDetails(WasherUser user) {
    return User.builder()
      .username(user.getEmail())
      .password(user.getPassword())
      .authorities("ROLE_" + user.getRole().name())
      .disabled(!user.isEnabled())
      .build();
  }

  private AuthResponse toResponse(WasherUser user, String token) {
    return new AuthResponse(
      user.getId(),
      user.getName(),
      user.getEmail(),
      user.getRole().name(),
      token
    );
  }
}

//package com.servicewasher.backend.auth;
//
//import com.servicewasher.backend.auth.dto.AuthResponse;
//import com.servicewasher.backend.auth.dto.LoginRequest;
//import com.servicewasher.backend.auth.dto.RegisterRequest;
//import com.servicewasher.backend.security.JwtService;
//import com.servicewasher.backend.user.UserRole;
//import com.servicewasher.backend.user.WasherUser;
//import com.servicewasher.backend.repository.WasherUserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//  private final WasherUserRepository userRepository;
//  private final PasswordEncoder passwordEncoder;
//  private final AuthenticationManager authenticationManager;
//  private final JwtService jwtService;
//
//  public AuthResponse register(RegisterRequest request) {
//    String email = request.email().trim().toLowerCase();
//    if (userRepository.existsByEmail(email)) {
//      throw new RuntimeException("Email already registered");
//    }
//
//    WasherUser user = new WasherUser();
//    user.setName(request.name());
//    user.setEmail(email);
//    user.setPassword(passwordEncoder.encode(request.password()));
//    user.setRole(UserRole.WASHER);
//    user.setEnabled(true);
//
//    WasherUser saved = userRepository.save(user);
//    String token = jwtService.generateToken(toUserDetails(saved));
//    return toResponse(saved, token);
//  }
//
//  public AuthResponse login(LoginRequest request) {
//    String email = request.email().trim().toLowerCase();
//
//    authenticationManager.authenticate(
//      new UsernamePasswordAuthenticationToken(email, request.password())
//    );
//
//    WasherUser user = userRepository.findByEmail(email)
//      .orElseThrow(() -> new RuntimeException("Invalid credentials"));
//
//    String token = jwtService.generateToken(toUserDetails(user));
//    return toResponse(user, token);
//  }
//
//  public AuthResponse me(String email) {
//    WasherUser user = userRepository.findByEmail(email)
//      .orElseThrow(() -> new RuntimeException("User not found"));
//    return toResponse(user, null);
//  }
//
//  private UserDetails toUserDetails(WasherUser user) {
//    return User.builder()
//      .username(user.getEmail())
//      .password(user.getPassword())
//      .authorities("ROLE_" + user.getRole().name())
//      .disabled(!user.isEnabled())
//      .build();
//  }
//
//  private AuthResponse toResponse(WasherUser user, String token) {
//    return new AuthResponse(
//      user.getId(),
//      user.getName(),
//      user.getEmail(),
//      user.getRole().name(),
//      token
//    );
//  }
//}