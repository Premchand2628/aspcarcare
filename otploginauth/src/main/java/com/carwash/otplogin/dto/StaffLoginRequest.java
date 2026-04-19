package com.carwash.otplogin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffLoginRequest {
    private String phone;
    private String password;
}
