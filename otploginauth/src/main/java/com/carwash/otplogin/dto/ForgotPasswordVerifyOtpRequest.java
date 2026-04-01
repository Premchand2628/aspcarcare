package com.carwash.otplogin.dto;

import lombok.Data;

@Data
public class ForgotPasswordVerifyOtpRequest {
    private String email;
    private String otp;
}
