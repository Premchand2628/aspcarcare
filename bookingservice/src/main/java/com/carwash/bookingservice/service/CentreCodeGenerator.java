package com.carwash.bookingservice.service;

import com.carwash.bookingservice.repository.ServiceCentreRepository;
import org.springframework.stereotype.Service;

/**
 * Builds a human-readable centre code in the form
 * {3 letters of name}-{3 letters of area}-{pincode}, with a numeric
 * suffix (-2, -3, ...) when the base code already exists.
 *
 * Example: "ASP Care Miyapur" / "Miyapur" / "500049" -> ASP-MIY-500049.
 */
@Service
public class CentreCodeGenerator {

    private final ServiceCentreRepository repository;

    public CentreCodeGenerator(ServiceCentreRepository repository) {
        this.repository = repository;
    }

    public String generate(String name, String area, String pincode) {
        String base = String.format("%s-%s-%s",
                slug(name, 3),
                slug(area, 3),
                normalizePincode(pincode));

        // collision-resistant: append -2, -3, ... until unused
        String candidate = base;
        int suffix = 2;
        while (repository.existsByCentreCode(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
            if (suffix > 999) { // pathological safety net
                throw new IllegalStateException("Cannot generate unique centre_code from " + base);
            }
        }
        return candidate;
    }

    private static String slug(String src, int length) {
        if (src == null) return "XXX".substring(0, Math.min(3, length));
        String letters = src.replaceAll("[^A-Za-z]", "");
        if (letters.isEmpty()) return "XXX".substring(0, Math.min(3, length));
        String upper = letters.toUpperCase();
        return upper.length() <= length ? upper : upper.substring(0, length);
    }

    private static String normalizePincode(String pin) {
        if (pin == null) return "000000";
        String digits = pin.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? "000000" : digits;
    }
}
