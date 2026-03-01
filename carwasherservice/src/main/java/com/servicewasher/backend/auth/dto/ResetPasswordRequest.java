package com.servicewasher.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
  @Email(message = "Invalid email")
  @NotBlank(message = "Email is required")
  String email,

  String newPassword
) {}