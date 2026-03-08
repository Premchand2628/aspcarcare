package com.carwash.otplogin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private String token; // optional

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

     }

