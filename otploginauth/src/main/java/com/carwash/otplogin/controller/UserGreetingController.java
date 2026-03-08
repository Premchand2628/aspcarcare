package com.carwash.otplogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.CarwashUserRepository;
import com.carwashcommon.security.JwtUserPrincipal;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserGreetingController {

    @Autowired
    private CarwashUserRepository carwashUserRepository;

    @GetMapping("/greeting")
    public ResponseEntity<?> getGreeting(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }

            String identifier = extractIdentifier(authentication);
            if (identifier == null || identifier.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
            }

            // Fetch user from carwash_user table
            Optional<User> userOptional = Optional.empty();

            boolean looksLikeEmail = identifier.contains("@");

            if (looksLikeEmail) {
                userOptional = carwashUserRepository.findByEmail(identifier);
            } else {
                userOptional = carwashUserRepository.findByPhone(identifier);
            }

            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }

            User user = userOptional.get();
            String firstName = (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) 
                ? user.getFirstName() 
                : "User";

            // Determine greeting based on current system time
            LocalTime currentTime = LocalTime.now();
            int currentHour = currentTime.getHour();
            String greeting;

            if (currentHour >= 0 && currentHour < 12) {
                // 12:00 AM to 11:59 AM = Morning
                greeting = "Good Morning";
            } else if (currentHour >= 12 && currentHour < 17) {
                // 12:00 PM to 4:59 PM = Afternoon
                greeting = "Good Afternoon";
            } else {
                // 5:00 PM to 11:59 PM = Evening
                greeting = "Good Evening";
            }

            // Return greeting and first name
            Map<String, String> response = new HashMap<>();
            response.put("greeting", greeting);
            response.put("firstName", firstName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    private String extractIdentifier(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            return jwtUserPrincipal.getPhone();
        }

        if (principal instanceof String s) {
            return s;
        }

        return null;
    }
}