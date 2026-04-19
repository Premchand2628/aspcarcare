package com.carwash.otplogin.service;

import com.carwash.otplogin.dto.StaffLoginRequest;
import com.carwash.otplogin.dto.StaffRegisterRequest;
import com.carwash.otplogin.entity.CentreStaff;
import com.carwash.otplogin.repository.CentreStaffRepository;
import com.carwashcommon.security.JwtTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffAuthService {

    private final CentreStaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public Map<String, Object> login(StaffLoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        if (phone == null || phone.isBlank() || password == null || password.isBlank()) {
            return Map.of("success", false, "message", "Phone and password are required");
        }

        Optional<CentreStaff> optStaff = staffRepository.findByPhoneAndIsActiveTrue(phone.trim());
        if (optStaff.isEmpty()) {
            log.warn("Staff login failed: phone {} not found or inactive", phone);
            return Map.of("success", false, "message", "Invalid credentials");
        }

        CentreStaff staff = optStaff.get();

        if (!passwordEncoder.matches(password, staff.getPasswordHash())) {
            log.warn("Staff login failed: wrong password for phone {}", phone);
            return Map.of("success", false, "message", "Invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("CENTRE_STAFF"));
        claims.put("phone", staff.getPhone());
        claims.put("role", staff.getRole());
        claims.put("centreId", staff.getCentreId());
        claims.put("staffId", staff.getId().toString());

        String token = jwtTokenService.generateAccessToken(staff.getPhone(), claims);

        log.info("Staff login successful: phone={} role={} centreId={}", phone, staff.getRole(), staff.getCentreId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Login successful");
        result.put("token", token);
        result.put("centreId", staff.getCentreId());
        result.put("name", staff.getName());
        result.put("role", staff.getRole());
        result.put("address", staff.getAddress());
        result.put("mapsUrl", staff.getMapsUrl());
        result.put("rating", staff.getRating());
        return result;
    }

    public Map<String, Object> register(StaffRegisterRequest request) {
        if (request.getPhone() == null || request.getPhone().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getName() == null || request.getName().isBlank()) {
            return Map.of("success", false, "message", "Phone, password, and name are required");
        }

        if (staffRepository.existsByPhone(request.getPhone().trim())) {
            return Map.of("success", false, "message", "Phone number already registered");
        }

        CentreStaff staff = new CentreStaff();
        staff.setPhone(request.getPhone().trim());
        staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        staff.setName(request.getName().trim());
        String role = request.getRole() != null ? request.getRole().trim().toLowerCase() : "washer";
        if (!List.of("washer", "manager", "supervisor").contains(role)) {
            return Map.of("success", false, "message", "Invalid role. Allowed: washer, manager, supervisor");
        }
        staff.setRole(role);
        staff.setCentreId(request.getCentreId());
        staff.setAddress(request.getAddress());
        staff.setMapsUrl(request.getMapsUrl());
        staff.setRating(request.getRating());
        staff.setIsActive(true);

        staffRepository.save(staff);

        log.info("Staff registered: phone={} name={} role={}", staff.getPhone(), staff.getName(), staff.getRole());

        return Map.of(
                "success", true,
                "message", "Staff registered successfully",
                "staffId", staff.getId().toString()
        );
    }

    public List<CentreStaff> listAllStaff() {
        return staffRepository.findAll();
    }

    public Map<String, Object> toggleActive(String staffId) {
        try {
            UUID id = UUID.fromString(staffId);
            Optional<CentreStaff> opt = staffRepository.findById(id);
            if (opt.isEmpty()) {
                return Map.of("success", false, "message", "Staff not found");
            }
            CentreStaff staff = opt.get();
            staff.setIsActive(!staff.getIsActive());
            staffRepository.save(staff);
            log.info("Staff {} toggled active={}", staff.getPhone(), staff.getIsActive());
            return Map.of("success", true, "active", staff.getIsActive());
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "message", "Invalid staff ID");
        }
    }
}
