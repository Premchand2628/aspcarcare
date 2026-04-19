package com.carwash.otplogin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffRegisterRequest {
    private String phone;
    private String password;
    private String name;
    private String role;
    private Long centreId;
    private String address;
    private String mapsUrl;
    private BigDecimal rating;
}
