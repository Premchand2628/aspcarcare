package com.carwash.bookingservice.client.MembershipClient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
}
