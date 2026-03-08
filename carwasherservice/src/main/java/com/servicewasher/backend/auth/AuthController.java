package com.servicewasher.backend.auth;

import com.servicewasher.backend.auth.dto.AuthResponse;
import com.servicewasher.backend.auth.dto.LoginRequest;
import com.servicewasher.backend.auth.dto.RegisterRequest;
import com.servicewasher.backend.auth.dto.ResetPasswordRequest;
import com.servicewasher.backend.auth.dto.ResetPasswordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @GetMapping("/me")
  public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(authService.me(userDetails.getUsername()));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(authService.resetPassword(request));
  }
}
//package com.servicewasher.backend.auth;
//
//import com.servicewasher.backend.auth.dto.AuthResponse;
//import com.servicewasher.backend.auth.dto.LoginRequest;
//import com.servicewasher.backend.auth.dto.RegisterRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//  private final AuthService authService;
//
//  @PostMapping("/register")
//  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
//    return ResponseEntity.ok(authService.register(request));
//  }
//
//  @PostMapping("/login")
//  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
//    return ResponseEntity.ok(authService.login(request));
//  }
//
//  @GetMapping("/me")
//  public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
//    return ResponseEntity.ok(authService.me(userDetails.getUsername()));
//  }
//}