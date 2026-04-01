package com.carwash.otplogin.dto;

import lombok.Data;

@Data
public class ProfileEmailVerifyOtpRequest {
    private String email;
    private String otp;
}
