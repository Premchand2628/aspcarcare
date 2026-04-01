package com.carwash.otplogin.dto;

import lombok.Data;

@Data
public class ForgotPasswordResetRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
}
