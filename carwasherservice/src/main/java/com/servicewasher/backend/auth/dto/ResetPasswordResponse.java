package com.servicewasher.backend.auth.dto;

public record ResetPasswordResponse(
  String message,
  String temporaryPassword
) {}