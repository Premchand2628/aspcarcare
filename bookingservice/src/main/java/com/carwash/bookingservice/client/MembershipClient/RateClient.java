package com.carwash.bookingservice.client.MembershipClient;

import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class RateClient {

  private final RestTemplate restTemplate = new RestTemplate();

  // ✅ Change port if your rates service runs on a different port
  private final String BASE = "http://localhost:8086/rates";

  private String enc(String v) {
    return URLEncoder.encode(v, StandardCharsets.UTF_8);
  }

  private String normalizeVehicleType(String carType) {
    if (carType == null || carType.isBlank()) return "HATCHBACK";
    return carType.trim().toUpperCase();
  }

  private String normalizeWashLevel(String washType) {
    if (washType == null || washType.isBlank()) return "BASIC";
    String wt = washType.trim().toUpperCase();
    if (wt.contains("PREMIUM")) return "PREMIUM";
    if (wt.contains("FOAM")) return "FOAM";
    return "BASIC";
  }

  @SuppressWarnings("unchecked")
  public BigDecimal getAmount(String carType, String washType) {
    String vt = normalizeVehicleType(carType);
    String wl = normalizeWashLevel(washType);

    String url = BASE + "?vehicleType=" + enc(vt) + "&washLevel=" + enc(wl);

    // ✅ Propagate MDC -> headers (correlation + txn)
    HttpHeaders headers = new HttpHeaders();

    String correlationId = MDC.get("correlationId");
    if (correlationId != null && !correlationId.isBlank()) {
      headers.set("X-Correlation-Id", correlationId);
    }

    String txnId = MDC.get("txnId");
    if (txnId != null && !txnId.isBlank()) {
      headers.set("X-Txn-Id", txnId);
    }

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

    if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
      throw new RuntimeException("Rate not found for vehicleType=" + vt + ", washLevel=" + wl);
    }

    Object amountObj = res.getBody().get("amount");
    if (amountObj == null) {
      throw new RuntimeException("Rate response missing amount");
    }

    return new BigDecimal(String.valueOf(amountObj));
  }
}
//package com.carwash.bookingservice.client.MembershipClient;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
//@Component
//public class RateClient {
//
//  private final RestTemplate restTemplate = new RestTemplate();
//
//  // ✅ Change port if your rates service runs on a different port
//  private final String BASE = "http://localhost:8086/rates";
//
//  private String enc(String v) {
//    return URLEncoder.encode(v, StandardCharsets.UTF_8);
//  }
//
//  private String normalizeVehicleType(String carType) {
//    if (carType == null || carType.isBlank()) return "HATCHBACK";
//    return carType.trim().toUpperCase();
//  }
//
//  private String normalizeWashLevel(String washType) {
//    if (washType == null || washType.isBlank()) return "BASIC";
//    String wt = washType.trim().toUpperCase();
//    if (wt.contains("PREMIUM")) return "PREMIUM";
//    if (wt.contains("FOAM")) return "FOAM";
//    return "BASIC";
//  }
//
//  @SuppressWarnings("unchecked")
//  public BigDecimal getAmount(String carType, String washType) {
//    String vt = normalizeVehicleType(carType);
//    String wl = normalizeWashLevel(washType);
//
//    String url = BASE + "?vehicleType=" + enc(vt) + "&washLevel=" + enc(wl);
//
//    ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
//    if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
//      throw new RuntimeException("Rate not found for vehicleType=" + vt + ", washLevel=" + wl);
//    }
//
//    Object amountObj = res.getBody().get("amount");
//    if (amountObj == null) {
//      throw new RuntimeException("Rate response missing amount");
//    }
//
//    return new BigDecimal(String.valueOf(amountObj));
//  }
//}
