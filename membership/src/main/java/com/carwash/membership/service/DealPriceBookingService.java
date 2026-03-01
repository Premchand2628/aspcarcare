package com.carwash.membership.service;

import com.carwash.membership.dto.DealPriceBookingCreateRequest;
import com.carwash.membership.entity.DealPriceBooking;
import com.carwash.membership.repository.DealPriceBookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    return dealPriceBookingRepository.save(booking);
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
    return "Y".equals(normalized) ? "Y" : "N";
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
}
