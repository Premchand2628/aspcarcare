package com.carwash.membership.service;

import com.carwash.membership.dto.DealPriceBookingCreateRequest;
import com.carwash.membership.dto.DealPriceBookingRedeemRequest;
import com.carwash.membership.entity.DealPriceBooking;
import com.carwash.membership.repository.DealPriceBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DealPriceBookingService {

  private final DealPriceBookingRepository dealPriceBookingRepository;

  public DealPriceBookingService(DealPriceBookingRepository dealPriceBookingRepository) {
    this.dealPriceBookingRepository = dealPriceBookingRepository;
  }

  public DealPriceBooking createBooking(DealPriceBookingCreateRequest request, String resolvedPhone) {
    if (request == null) {
      throw new IllegalArgumentException("Request is required");
    }

    String phone = normalizePhone(resolvedPhone, request.getPhone());
    String carType = required(request.getCarType(), "carType");
    String serviceType = required(request.getServiceType(), "serviceType");
    String washType = required(request.getWashType(), "washType");
    String waterProvided = normalizeWaterFlag(request.getWaterProvided());
    int totalWashes = resolveTotalWashes(request.getTotalWashes(), washType);

    DealPriceBooking booking = new DealPriceBooking();
    booking.setPhone(phone);
    booking.setCarType(carType);
    booking.setServiceType(serviceType);
    booking.setWashType(washType);
    booking.setWaterProvided(waterProvided);
    booking.setPaymentStatus(blankToDefault(request.getPaymentStatus(), "SUCCESS"));
    booking.setRefundAmount(request.getRefundAmount() == null ? BigDecimal.ZERO : request.getRefundAmount());
    booking.setRefundInitiatedAt(request.getRefundInitiatedAt());
    booking.setRefundStatus(blankToDefault(request.getRefundStatus(), "NOT_INITIATED"));
    booking.setTransactionId(blankToNull(request.getTransactionId()));
    booking.setDiscountPercentApplied(safeBigDecimal(request.getDiscountPercentApplied()));
    booking.setOriginalAmount(safeBigDecimal(request.getOriginalAmount()));
    booking.setPayableAmount(safeBigDecimal(request.getPayableAmount()));
    booking.setPlanTypeCode(generatePlanTypeCode(carType, serviceType, washType, waterProvided));
    booking.setTotalWashes(totalWashes);
    booking.setUsedWashes(0);
    booking.setLeftWashes(totalWashes);

    return dealPriceBookingRepository.save(booking);
  }

  public List<DealPriceBooking> getBookingsForPhone(String phone) {
    String normalizedPhone = blankToNull(phone);
    if (normalizedPhone == null) {
      return List.of();
    }
    List<DealPriceBooking> bookings = dealPriceBookingRepository.findAllByPhoneOrderByCreatedAtDesc(normalizedPhone);
    bookings.forEach(this::normalizeWashTracking);
    return bookings;
  }

  @Transactional
  public DealPriceBooking redeemForBooking(DealPriceBookingRedeemRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Request is required");
    }

    String phone = required(request.getPhone(), "phone");
    String planTypeCode = required(request.getPlanTypeCode(), "planTypeCode");
    String carType = required(request.getCarType(), "carType");
    String serviceType = required(request.getServiceType(), "serviceType");
    String washType = required(request.getWashType(), "washType");

    List<DealPriceBooking> matchedByPlan = dealPriceBookingRepository
        .findByPhoneAndPlanTypeCodeOrderByCreatedAtDesc(phone, planTypeCode);

    if (matchedByPlan.isEmpty()) {
      throw new IllegalArgumentException("No subscription found for plan code");
    }

    DealPriceBooking matched = matchedByPlan.stream()
        .filter(item -> matches(item.getCarType(), carType)
            && matches(item.getServiceType(), serviceType)
            && matchesWashType(item.getWashType(), washType))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Subscription details do not match selected booking"));

    int used = matched.getUsedWashes() == null ? 0 : matched.getUsedWashes();
    int left = matched.getLeftWashes() == null ? Math.max((matched.getTotalWashes() == null ? 3 : matched.getTotalWashes()) - used, 0) : matched.getLeftWashes();

    if (left <= 0) {
      throw new IllegalArgumentException("No washes left for this subscription");
    }

    int updatedRows = dealPriceBookingRepository.consumeWashIfAvailable(matched.getId());
    if (updatedRows == 0) {
      throw new IllegalArgumentException("No washes left for this subscription");
    }

    return dealPriceBookingRepository.findById(matched.getId())
        .orElseThrow(() -> new IllegalStateException("Subscription not found after redemption"));
  }

  private String normalizePhone(String resolvedPhone, String requestedPhone) {
    String phone = blankToNull(resolvedPhone);
    if (phone == null) {
      phone = blankToNull(requestedPhone);
    }
    if (phone == null) {
      throw new IllegalArgumentException("phone is required");
    }
    return phone;
  }

  private String required(String value, String field) {
    String normalized = blankToNull(value);
    if (normalized == null) {
      throw new IllegalArgumentException(field + " is required");
    }
    return normalized;
  }

  private String normalizeWaterFlag(String value) {
    String normalized = blankToDefault(value, "N").toUpperCase(Locale.ROOT);
    if ("Y".equals(normalized) || "YES".equals(normalized)) {
      return "Y";
    }
    return "N";
  }

  private BigDecimal safeBigDecimal(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String blankToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String blankToDefault(String value, String defaultValue) {
    String trimmed = blankToNull(value);
    return trimmed == null ? defaultValue : trimmed;
  }

  private String generatePlanTypeCode(String carType, String serviceType, String washType, String waterFlag) {
    String carPart = firstTwoLetters(carType);
    String servicePart = firstTwoLetters(serviceType);

    String washPart = Arrays.stream(washType.split("[^A-Za-z]+"))
        .map(this::firstTwoLetters)
        .filter(Objects::nonNull)
        .collect(Collectors.joining());

    if (washPart.isBlank()) {
      washPart = firstTwoLetters(washType);
    }

    return (carPart + servicePart + washPart + "WF" + normalizeWaterFlag(waterFlag)).toUpperCase(Locale.ROOT);
  }

  private int resolveTotalWashes(Integer requestedTotalWashes, String washType) {
    if (requestedTotalWashes != null && requestedTotalWashes > 0) {
      return requestedTotalWashes;
    }

    if (washType != null && washType.toUpperCase(Locale.ROOT).contains("+")) {
      return 3;
    }

    return 3;
  }

  private void normalizeWashTracking(DealPriceBooking booking) {
    int total = booking.getTotalWashes() == null || booking.getTotalWashes() <= 0
        ? 3
        : booking.getTotalWashes();

    int used = booking.getUsedWashes() == null || booking.getUsedWashes() < 0
        ? 0
        : booking.getUsedWashes();

    int left = booking.getLeftWashes() == null || booking.getLeftWashes() < 0
        ? Math.max(total - used, 0)
        : booking.getLeftWashes();

    booking.setTotalWashes(total);
    booking.setUsedWashes(used);
    booking.setLeftWashes(left);
  }

  private String firstTwoLetters(String value) {
    if (value == null) {
      return "";
    }

    String lettersOnly = value.replaceAll("[^A-Za-z]", "").toUpperCase(Locale.ROOT);
    if (lettersOnly.isEmpty()) {
      return "";
    }
    return lettersOnly.substring(0, Math.min(2, lettersOnly.length()));
  }

  private boolean matches(String actual, String expected) {
    String a = blankToDefault(actual, "").replace("_", " ").trim().toUpperCase(Locale.ROOT);
    String e = blankToDefault(expected, "").replace("_", " ").trim().toUpperCase(Locale.ROOT);

    if (a.equals(e)) return true;
    if ((a.equals("SELFDRIVE") || a.equals("SELF DRIVE")) && (e.equals("SELFDRIVE") || e.equals("SELF DRIVE"))) {
      return true;
    }
    return false;
  }

  private boolean matchesWashType(String actual, String expected) {
    String a = blankToDefault(actual, "").trim().toUpperCase(Locale.ROOT);
    String e = blankToDefault(expected, "").trim().toUpperCase(Locale.ROOT);
    return a.equals(e);
  }
}
