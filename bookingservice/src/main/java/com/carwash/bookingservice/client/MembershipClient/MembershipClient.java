package com.carwash.bookingservice.client.MembershipClient;

import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
=======
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

<<<<<<< HEAD
import com.carwash.bookingservice.dto.DealPriceBookingRedeemRequest;

=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
@Component
public class MembershipClient {

  private final RestTemplate restTemplate = new RestTemplate();

  // ✅ FIX: membership-service runs on 8085 (NOT 8080)
  private final String BASE = "http://localhost:8085/memberships";

  private String enc(String v) {
    return URLEncoder.encode(v, StandardCharsets.UTF_8);
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> preview(String phone, BigDecimal amount, String washType) {
    String url = BASE + "/benefits/preview?phone=" + enc(phone)
        + "&amount=" + enc(String.valueOf(amount));

    if (washType != null && !washType.isBlank()) {
      url += "&washType=" + enc(washType.trim());
    }

    ResponseEntity<Map> res = restTemplate.getForEntity(url, Map.class);
    return res.getBody();
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> apply(String phone, BigDecimal amount, String washType, String bookingTxnId) {
    String url = BASE + "/benefits/apply?phone=" + enc(phone)
        + "&amount=" + enc(String.valueOf(amount));

    if (washType != null && !washType.isBlank()) {
      url += "&washType=" + enc(washType.trim());
    }

    if (bookingTxnId != null && !bookingTxnId.isBlank()) {
      url += "&bookingTxnId=" + enc(bookingTxnId.trim());
    }

    ResponseEntity<Map> res = restTemplate.postForEntity(url, null, Map.class);
    return res.getBody();
  }

  /**
   * ✅ This endpoint is in MembershipController:
   * POST /memberships/{membershipDbId}/consume-free?washType=FOAM&amount=400
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> consumeFree(Long membershipDbId, String washType, BigDecimal amount) {
    if (membershipDbId == null) throw new IllegalArgumentException("membershipDbId is required");
    if (washType == null || washType.isBlank()) throw new IllegalArgumentException("washType is required");

    String url = BASE + "/" + enc(String.valueOf(membershipDbId))
        + "/consume-free?washType=" + enc(washType.trim());

    if (amount != null) {
      url += "&amount=" + enc(String.valueOf(amount));
    }

    ResponseEntity<Map> res = restTemplate.postForEntity(url, null, Map.class);
    return res.getBody();
  }
<<<<<<< HEAD

  @SuppressWarnings("unchecked")
  public Map<String, Object> redeemDealSubscription(DealPriceBookingRedeemRequest request) {
    String url = BASE + "/deal-price-bookings/redeem";
    HttpHeaders headers = new HttpHeaders();
    String authHeader = resolveAuthorizationHeader();
    if (authHeader != null && !authHeader.isBlank()) {
      headers.set(HttpHeaders.AUTHORIZATION, authHeader);
    }

    HttpEntity<DealPriceBookingRedeemRequest> entity = new HttpEntity<>(request, headers);
    ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
    return res.getBody();
  }

  private String resolveAuthorizationHeader() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
      return null;
    }
    return servletAttrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
  }
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
}
