package com.carwash.otplogin.controller;

import com.carwash.otplogin.dto.ApiResponse;
import com.carwash.otplogin.dto.UserAddressRequest;
import com.carwash.otplogin.dto.UserAddressResponse;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.entity.UserAddress;
import com.carwash.otplogin.repository.UserAddressRepository;
import com.carwash.otplogin.repository.UserRepository;
import com.carwashcommon.security.JwtUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Endpoints for managing the saved-address book of the currently authenticated user.
 *
 * All routes are JWT-protected by the global SecurityFilterChain
 * (anyRequest().authenticated()) — only /users/signup is public on this prefix.
 */
@RestController
@RequestMapping("/users/addresses")
public class UserAddressController {

    private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{6}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    public UserAddressController(UserAddressRepository addressRepository,
                                 UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private String getPhoneFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal p) {
            String candidate = p.getPhone();
            if (candidate == null || candidate.isBlank()) return null;
            candidate = candidate.trim();
            if (candidate.contains("@")) {
                Optional<User> byEmail = userRepository.findByEmail(candidate);
                if (byEmail.isPresent() && byEmail.get().getPhone() != null && !byEmail.get().getPhone().isBlank()) {
                    return byEmail.get().getPhone();
                }
                if (p.getClaims() != null) {
                    Object phoneFromClaim = p.getClaims().get("phone");
                    if (phoneFromClaim != null) {
                        String claimPhone = String.valueOf(phoneFromClaim).trim();
                        if (!claimPhone.isBlank()) return claimPhone;
                    }
                }
            }
            return candidate;
        }
        if (principal instanceof String s && !s.isBlank()) {
            return s.trim();
        }
        return null;
    }

    private Optional<User> resolveUser(Authentication authentication) {
        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) return Optional.empty();
        Optional<User> byPhone = userRepository.findByPhone(phone);
        if (byPhone.isPresent()) return byPhone;
        if (phone.contains("@")) return userRepository.findByEmail(phone);
        return Optional.empty();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static String requireField(String value, String fieldName, java.util.List<String> errors) {
        String t = trimToNull(value);
        if (t == null) {
            errors.add(fieldName + " is required");
            return null;
        }
        return t;
    }

    private static ResponseEntity<ApiResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ApiResponse(false, message));
    }

    private static ResponseEntity<ApiResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
    }

    private static ResponseEntity<ApiResponse> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, message));
    }

    private void applyRequestToEntity(UserAddressRequest req, UserAddress entity, java.util.List<String> errors) {
        String label = trimToNull(req.getLabel());
        entity.setLabel(label == null ? "Home" : label);

        entity.setFullName(trimToNull(req.getFullName()));      // optional
        String phone = trimToNull(req.getPhone());
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.add("Phone number must be exactly 10 digits");
        } else {
            entity.setPhone(phone);
        }

        String zipcode = requireField(req.getZipcode(), "Zipcode", errors);
        if (zipcode != null) {
            if (!ZIP_PATTERN.matcher(zipcode).matches()) {
                errors.add("Zipcode must be exactly 6 digits");
            } else {
                entity.setZipcode(zipcode);
            }
        }

        String area = requireField(req.getArea(), "Area", errors);
        if (area != null) entity.setArea(area);

        String street = requireField(req.getStreetAddress(), "Street address", errors);
        if (street != null) entity.setStreetAddress(street);

        String city = requireField(req.getCity(), "City", errors);
        if (city != null) entity.setCity(city);

        String state = requireField(req.getState(), "State", errors);
        if (state != null) entity.setState(state);

        entity.setLandmark(trimToNull(req.getLandmark()));      // optional
        entity.setLatitude(req.getLatitude());
        entity.setLongitude(req.getLongitude());
    }

    // -------------------------------------------------
    // Endpoints
    // -------------------------------------------------

    /** GET /users/addresses — list active addresses for the authenticated user. */
    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) return unauthorized();
        Long userId = userOpt.get().getId();

        List<UserAddressResponse> data = addressRepository
                .findByUserIdAndActiveTrueOrderByDefaultAddressDescCreatedAtAsc(userId)
                .stream()
                .map(UserAddressResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "data", data
        ));
    }

    /** POST /users/addresses — create a new saved address. */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody UserAddressRequest req, Authentication authentication) {
        if (req == null) return badRequest("Request body is required");

        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) return unauthorized();
        Long userId = userOpt.get().getId();

        UserAddress entity = new UserAddress();
        entity.setUserId(userId);
        entity.setActive(true);

        java.util.List<String> errors = new java.util.ArrayList<>();
        applyRequestToEntity(req, entity, errors);
        if (!errors.isEmpty()) return badRequest(String.join("; ", errors));

        boolean wantsDefault = Boolean.TRUE.equals(req.getDefaultAddress());
        boolean isFirst = addressRepository
                .findByUserIdAndActiveTrueOrderByDefaultAddressDescCreatedAtAsc(userId)
                .isEmpty();
        entity.setDefaultAddress(wantsDefault || isFirst);

        UserAddress saved = addressRepository.save(entity);
        if (saved.isDefaultAddress()) {
            addressRepository.clearOtherDefaults(userId, saved.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                "success", true,
                "message", "Address saved",
                "data", UserAddressResponse.from(saved)
        ));
    }

    /** PUT /users/addresses/{id} — update an existing saved address. */
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody UserAddressRequest req,
                                    Authentication authentication) {
        if (req == null) return badRequest("Request body is required");

        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) return unauthorized();
        Long userId = userOpt.get().getId();

        Optional<UserAddress> opt = addressRepository.findByIdAndUserIdAndActiveTrue(id, userId);
        if (opt.isEmpty()) return notFound("Address not found");

        UserAddress entity = opt.get();

        java.util.List<String> errors = new java.util.ArrayList<>();
        applyRequestToEntity(req, entity, errors);
        if (!errors.isEmpty()) return badRequest(String.join("; ", errors));

        if (Boolean.TRUE.equals(req.getDefaultAddress())) {
            entity.setDefaultAddress(true);
        }

        UserAddress saved = addressRepository.save(entity);
        if (saved.isDefaultAddress()) {
            addressRepository.clearOtherDefaults(userId, saved.getId());
        }

        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Address updated",
                "data", UserAddressResponse.from(saved)
        ));
    }

    /** DELETE /users/addresses/{id} — soft-delete a saved address. */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication authentication) {
        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) return unauthorized();
        Long userId = userOpt.get().getId();

        Optional<UserAddress> opt = addressRepository.findByIdAndUserIdAndActiveTrue(id, userId);
        if (opt.isEmpty()) return notFound("Address not found");

        UserAddress entity = opt.get();
        boolean wasDefault = entity.isDefaultAddress();
        entity.setActive(false);
        entity.setDefaultAddress(false);
        addressRepository.save(entity);

        // If we just removed the default, promote the most-recent active address (if any) to default.
        if (wasDefault) {
            List<UserAddress> remaining = addressRepository
                    .findByUserIdAndActiveTrueOrderByDefaultAddressDescCreatedAtAsc(userId);
            if (!remaining.isEmpty()) {
                UserAddress promote = remaining.get(0);
                promote.setDefaultAddress(true);
                addressRepository.save(promote);
                addressRepository.clearOtherDefaults(userId, promote.getId());
            }
        }

        return ResponseEntity.ok(new ApiResponse(true, "Address deleted"));
    }

    /** PATCH /users/addresses/{id}/default — mark this address as the user's default. */
    @PatchMapping("/{id}/default")
    @Transactional
    public ResponseEntity<?> setDefault(@PathVariable Long id, Authentication authentication) {
        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) return unauthorized();
        Long userId = userOpt.get().getId();

        Optional<UserAddress> opt = addressRepository.findByIdAndUserIdAndActiveTrue(id, userId);
        if (opt.isEmpty()) return notFound("Address not found");

        addressRepository.clearAllDefaults(userId);
        UserAddress entity = opt.get();
        entity.setDefaultAddress(true);
        UserAddress saved = addressRepository.save(entity);

        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", "Default address updated",
                "data", UserAddressResponse.from(saved)
        ));
    }
}
