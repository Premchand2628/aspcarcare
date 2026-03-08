package com.carwash.membership.controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.carwash.membership.dto.MembershipBenefitResponse;
import com.carwash.membership.dto.MembershipCreateRequest;
import com.carwash.membership.entity.Membership;
import com.carwash.membership.service.MembershipService;
import com.carwash.membership.repository.*;
import com.carwashcommon.security.JwtUserPrincipal;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/memberships")
@CrossOrigin(origins = "*")
public class MembershipController {

  @Value("${spring.datasource.url}")
  private String datasourceUrl;

  @PostConstruct
  public void printDb() {
    System.out.println("CONNECTED DB = " + datasourceUrl);
  }

  private final MembershipService membershipService;
  private final MembershipRepository membershipRepository;

  public MembershipController(MembershipService membershipService, MembershipRepository membershipRepository) {
    this.membershipService = membershipService;
    this.membershipRepository = membershipRepository;
  }

//  @GetMapping("/by-phone")
//  public ResponseEntity<Membership> getByPhone(@RequestParam String phone) {
//  @GetMapping("/by-phone")
//  public ResponseEntity<Membership> getByPhone(@RequestParam("phone") String phone){
//    return membershipService.getLatestByPhone(phone)
//        .map(ResponseEntity::ok)
//        .orElseGet(() -> ResponseEntity.notFound().build());
//  }
  @GetMapping("/by-phone")
  public ResponseEntity<List<Membership>> getAllMembershipsByPhone(@RequestParam String phone, Authentication authentication) {
      String resolvedPhone = resolvePhone(authentication);
      if (resolvedPhone == null || resolvedPhone.isBlank()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      if (phone == null || phone.isBlank()) {
        return ResponseEntity.badRequest().build();
      }

      if (!resolvedPhone.equals(phone.trim())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      List<Membership> memberships = membershipRepository.findAllByPhone(resolvedPhone);
      // Sort by createdAt descending (newest first)
      memberships.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
      return ResponseEntity.ok(memberships);
  }

  @GetMapping("/active/by-phone")
  public ResponseEntity<Membership> getActiveByPhone(@RequestParam("phone") String phone, Authentication authentication) {
    String resolvedPhone = resolvePhone(authentication);
    if (resolvedPhone == null || resolvedPhone.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    if (phone == null || phone.isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    if (!resolvedPhone.equals(phone.trim())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return membershipService.getLatestActiveByPhone(resolvedPhone)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/me")
  public ResponseEntity<List<Membership>> getAllMembershipsForMe(Authentication authentication) {
    String resolvedPhone = resolvePhone(authentication);
    if (resolvedPhone == null || resolvedPhone.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    List<Membership> memberships = membershipRepository.findAllByPhone(resolvedPhone);
    memberships.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    return ResponseEntity.ok(memberships);
  }

  @GetMapping("/active/me")
  public ResponseEntity<Membership> getActiveForMe(Authentication authentication) {
    String resolvedPhone = resolvePhone(authentication);
    if (resolvedPhone == null || resolvedPhone.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return membershipService.getLatestActiveByPhone(resolvedPhone)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Membership> createMembership(@RequestBody MembershipCreateRequest request) {
    try {
      return ResponseEntity.ok(membershipService.createMembership(request));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping("/{membershipId}/status")
  public ResponseEntity<Membership> updateStatus(@PathVariable String membershipId,
                                                 @RequestParam("value") String value) {
    try {
      return ResponseEntity.ok(membershipService.updateStatus(membershipId, value));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/benefits/preview")
  public ResponseEntity<MembershipBenefitResponse> previewBenefit(@RequestParam String phone,
                                                                  @RequestParam BigDecimal amount,
                                                                  @RequestParam(required = false) String washType) {
    return ResponseEntity.ok(membershipService.previewBenefit(phone, amount, washType));
  }

  @PostMapping("/benefits/apply")
  public ResponseEntity<MembershipBenefitResponse> applyBenefit(@RequestParam String phone,
                                                                @RequestParam BigDecimal amount,
                                                                @RequestParam(required = false) String washType,
                                                                @RequestParam(required = false) String bookingTxnId) {
    return ResponseEntity.ok(membershipService.applyBenefit(phone, amount, washType, bookingTxnId));
  }

  @PostMapping("/{membershipDbId}/consume-free")
  public ResponseEntity<MembershipBenefitResponse> consumeFreeDirect(@PathVariable Long membershipDbId,
                                                                     @RequestParam String washType,
                                                                     @RequestParam(required = false) BigDecimal amount,
                                                                     @RequestParam(required = false) String transactionId) {
    return ResponseEntity.ok(membershipService.consumeFreeDirect(membershipDbId, washType, amount, transactionId));
  }

  private String resolvePhone(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
      Claims claims = jwtUserPrincipal.getClaims();
      Object phoneClaim = claims != null ? claims.get("phone") : null;

      if (phoneClaim != null && !phoneClaim.toString().isBlank()) {
        return phoneClaim.toString();
      }

      String subject = jwtUserPrincipal.getPhone();
      if (subject != null && !subject.contains("@")) {
        return subject;
      }

      return null;
    }

    if (principal instanceof String s && !s.contains("@")) {
      return s;
    }

    return null;
  }
}
//package com.carwash.membership.controller;
//
//import com.carwash.membership.dto.MembershipBenefitResponse;
//import com.carwash.membership.dto.MembershipCreateRequest;
//import com.carwash.membership.entity.Membership;
//import com.carwash.membership.repository.MembershipRepository;
//
//import jakarta.annotation.PostConstruct;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.security.SecureRandom;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/memberships")
//@CrossOrigin(origins = "*")
//public class MembershipController {
//
//  @Value("${spring.datasource.url}")
//  private String datasourceUrl;
//
//  @PostConstruct
//  public void printDb() {
//    System.out.println("CONNECTED DB = " + datasourceUrl);
//  }
//
//  private static final Logger log = LoggerFactory.getLogger(MembershipController.class);
//  private final MembershipRepository membershipRepository;
//
//  private final SecureRandom random = new SecureRandom();
//  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//
//  public MembershipController(MembershipRepository membershipRepository) {
//    this.membershipRepository = membershipRepository;
//  }
//
//  private String generateMembershipId() {
//    StringBuilder sb = new StringBuilder(22);
//    for (int i = 0; i < 22; i++) {
//      sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
//    }
//    return sb.toString();
//  }
//
//  private String generateUniqueMembershipId() {
//    String id;
//    do { id = generateMembershipId(); }
//    while (membershipRepository.existsByMembershipId(id));
//    return id;
//  }
//
//  // ------------------- PLAN RULES -------------------
//
//  private int membershipEndMonths(String planCode) {
//    if (planCode == null) return 0;
//    switch (planCode.trim().toUpperCase()) {
//      case "BASIC": return 3;
//      case "PREMIUM": return 6;
//      case "ULTRA": return 6;
//      default: return 0;
//    }
//  }
//
//  private int discountMonths(String planCode) {
//    if (planCode == null) return 0;
//    switch (planCode.trim().toUpperCase()) {
//      case "BASIC": return 2;
//      case "PREMIUM": return 3;
//      case "ULTRA": return 6;
//      default: return 0;
//    }
//  }
//
//  private BigDecimal planDiscount(String planCode) {
//    if (planCode == null) return BigDecimal.ZERO;
//    switch (planCode.trim().toUpperCase()) {
//      case "BASIC": return new BigDecimal("10");
//      case "PREMIUM": return new BigDecimal("10");
//      case "ULTRA": return new BigDecimal("15");
//      default: return BigDecimal.ZERO;
//    }
//  }
//
//  private void initFreebiesAndResetUsage(Membership m) {
//    String p = (m.getPlanCode() == null) ? "" : m.getPlanCode().trim().toUpperCase();
//
//    m.setFreeFoamRemaining(0);
//    m.setFreePremiumRemaining(0);
//
//    if ("BASIC".equals(p)) {
//      m.setFreeFoamRemaining(1);
//      m.setFreePremiumRemaining(0);
//    } else if ("PREMIUM".equals(p)) {
//      m.setFreeFoamRemaining(2);
//      m.setFreePremiumRemaining(0);
//    } else if ("ULTRA".equals(p)) {
//      m.setFreeFoamRemaining(2);
//      m.setFreePremiumRemaining(1);
//    }
//
//    m.setFreeUsed1(null);
//    m.setFreeUsed2(null);
//    m.setFreeUsed3(null);
//  }
//
//  private boolean isActiveAndValid(Membership m) {
//    if (m == null) return false;
//    if (!"ACTIVE".equalsIgnoreCase(m.getStatus())) return false;
//    if (m.getEndDate() == null) return false;
//    return LocalDateTime.now().isBefore(m.getEndDate());
//  }
//
//  private String normalizeWashType(String washType) {
//    if (washType == null || washType.isBlank()) return "BASIC";
//    String wt = washType.trim().toUpperCase();
//    if (wt.contains("PREMIUM")) return "PREMIUM";
//    if (wt.contains("FOAM")) return "FOAM";
//    return "BASIC";
//  }
//
//  private boolean canConsumeFree(Membership m, String washType) {
//    String wt = normalizeWashType(washType);
//    if ("FOAM".equals(wt)) return m.getFreeFoamRemaining() != null && m.getFreeFoamRemaining() > 0;
//    if ("PREMIUM".equals(wt)) return m.getFreePremiumRemaining() != null && m.getFreePremiumRemaining() > 0;
//    return false;
//  }
//
//  private void recordFreeUse(Membership m, String usedType) {
//    if (m.getFreeUsed1() == null) m.setFreeUsed1(usedType);
//    else if (m.getFreeUsed2() == null) m.setFreeUsed2(usedType);
//    else if (m.getFreeUsed3() == null) m.setFreeUsed3(usedType);
//  }
//
//  private void consumeFree(Membership m, String washType) {
//    String wt = normalizeWashType(washType);
//    if ("FOAM".equals(wt)) {
//      m.setFreeFoamRemaining(Math.max(0, (m.getFreeFoamRemaining() == null ? 0 : m.getFreeFoamRemaining()) - 1));
//      recordFreeUse(m, "FOAM");
//    } else if ("PREMIUM".equals(wt)) {
//      m.setFreePremiumRemaining(Math.max(0, (m.getFreePremiumRemaining() == null ? 0 : m.getFreePremiumRemaining()) - 1));
//      recordFreeUse(m, "PREMIUM");
//    }
//  }
//
//  private MembershipBenefitResponse buildResponse(Membership m,
//                                                 BigDecimal amount,
//                                                 boolean freeApplied,
//                                                 BigDecimal discountPct,
//                                                 BigDecimal payable,
//                                                 String msg) {
//    MembershipBenefitResponse r = new MembershipBenefitResponse();
//    r.setEligible(m != null && isActiveAndValid(m));
//    r.setFreeApplied(freeApplied);
//    r.setDiscountPercent(discountPct);
//    r.setOriginalAmount(amount);
//    r.setPayableAmount(payable);
//    r.setMessage(msg);
//
//    if (m != null) {
//      r.setMembershipDbId(m.getId());
//      r.setMembershipId(m.getMembershipId());
//      r.setPlanCode(m.getPlanCode());
//    }
//    return r;
//  }
//
//  // ------------------- ENDPOINTS -------------------
//
//  /**
//   * ✅ Always return LATEST membership row for phone (upgrade should show new one)
//   */
//  @GetMapping("/by-phone")
//  public ResponseEntity<Membership> getByPhone(@RequestParam String phone) {
//    if (phone == null || phone.isBlank()) return ResponseEntity.badRequest().build();
//    return membershipRepository.findTopByPhoneOrderByUpdatedAtDesc(phone.trim())
//        .map(ResponseEntity::ok)
//        .orElseGet(() -> ResponseEntity.notFound().build());
//  }
//
//  /**
//   * ✅ Return latest ACTIVE membership
//   */
//  @GetMapping("/active/by-phone")
//  public ResponseEntity<Membership> getActiveByPhone(@RequestParam String phone) {
//    if (phone == null || phone.isBlank()) return ResponseEntity.badRequest().build();
//    return membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE")
//        .map(ResponseEntity::ok)
//        .orElseGet(() -> ResponseEntity.notFound().build());
//  }
//
//  /**
//   * ✅ Create NEW membership row always.
//   * - For NEW purchase -> normal new row
//   * - For UPGRADE_* -> new row with isUpgrade=true + upgradedFromMembershipId + previousPlanCode + difference
//   *
//   * Status should be HOLD after payment.
//   */
//  @PostMapping
//  public ResponseEntity<Membership> createMembership(@RequestBody MembershipCreateRequest request) {
//
//    if (request == null || request.getPhone() == null || request.getPhone().isBlank()) {
//      return ResponseEntity.badRequest().build();
//    }
//
//    final String phone = request.getPhone().trim();
//    final String txnId = (request.getTransactionId() == null) ? null : request.getTransactionId().trim();
//
//    final String planCode = (request.getPlanCode() == null) ? "" : request.getPlanCode().trim().toUpperCase();
//    final String planName = (request.getPlanName() == null) ? "" : request.getPlanName().trim();
//
//    if (planCode.isBlank() || planName.isBlank()) {
//      return ResponseEntity.badRequest().build();
//    }
//
//    // IMPORTANT:
//    // priceToStore = full plan price (799/1599 etc)
//    BigDecimal priceToStore = request.getOriginalPlanPrice();
//    if (priceToStore == null) priceToStore = BigDecimal.ZERO;
//
//    // paidAmount = what user actually paid (gap or full)
//    BigDecimal paidAmount = request.getPaidAmount();
//    if (paidAmount == null) paidAmount = priceToStore;
//
//    String mode = (request.getPurchaseMode() == null) ? "NEW" : request.getPurchaseMode().trim().toUpperCase();
//    boolean isUpgrade = mode.startsWith("UPGRADE");
//
//    Membership m = new Membership();
//    m.setPhone(phone);
//    m.setMembershipId(generateUniqueMembershipId());
//    m.setPlanCode(planCode);
//    m.setPlanName(planName);
//
//    // ✅ store FULL plan price here
//    m.setPrice(priceToStore);
//
//    if (txnId != null && !txnId.isBlank()) m.setTransactionId(txnId);
//
//    // ✅ always HOLD after payment
//    m.setStatus("HOLD");
//
//    LocalDateTime now = LocalDateTime.now();
//    m.setStartDate(now);
//
//    int endMonths = membershipEndMonths(planCode);
//    m.setEndDate(endMonths > 0 ? now.plusMonths(endMonths) : null);
//
//    m.setDiscountPercent(planDiscount(planCode));
//    m.setDiscountStartDate(now);
//
//    int dm = discountMonths(planCode);
//    m.setDiscountEndDate(dm > 0 ? now.plusMonths(dm) : null);
//
//    initFreebiesAndResetUsage(m);
//
//    // ✅ Upgrade fields
//    if (isUpgrade) {
//      m.setIsUpgrade(true);
//      m.setUpgradeCreatedAt(now);
//
//      // prefer explicit source from UI
//      Long sourceDbId = request.getSourceMembershipDbId();
//      String prevPlan = request.getPreviousPlanCode();
//
//      // fallback: if UI didn't send, take latest membership as source
//      Membership source = null;
//      if (sourceDbId != null) {
//        source = membershipRepository.findById(sourceDbId).orElse(null);
//      }
//      if (source == null) {
//        source = membershipRepository.findTopByPhoneOrderByUpdatedAtDesc(phone).orElse(null);
//      }
//
//      if (source != null) {
//        m.setUpgradedFromMembershipId(source.getId());
//        if (prevPlan == null || prevPlan.isBlank()) prevPlan = source.getPlanCode();
//      }
//
//      if (prevPlan != null) m.setPreviousPlanCode(prevPlan.trim().toUpperCase());
//
//      BigDecimal diff = request.getUpgradeDifferenceAmount();
//      if (diff == null) diff = paidAmount; // gap upgrades => paidAmount
//      m.setUpgradeDifferenceAmount(diff);
//    } else {
//      m.setIsUpgrade(false);
//    }
//
//    Membership saved = membershipRepository.save(m);
//    return ResponseEntity.ok(saved);
//  }
//
//  @PutMapping("/{membershipId}/status")
//  public ResponseEntity<Membership> updateStatus(
//      @PathVariable String membershipId,
//      @RequestParam("value") String value
//  ) {
//    Optional<Membership> opt = membershipRepository.findByMembershipId(membershipId);
//    if (opt.isEmpty()) return ResponseEntity.notFound().build();
//
//    String newStatus = (value == null ? "" : value.trim().toUpperCase());
//    if (!(newStatus.equals("HOLD") || newStatus.equals("ACTIVE") || newStatus.equals("CANCELLED") || newStatus.equals("EXPIRED"))) {
//      return ResponseEntity.badRequest().build();
//    }
//
//    Membership m = opt.get();
//    m.setStatus(newStatus);
//
//    // NOTE: do not reset freebies here
//    // If you want startDate to begin only when ACTIVE, keep your earlier logic.
//    // For now, keep existing dates.
//
//    Membership saved = membershipRepository.save(m);
//    return ResponseEntity.ok(saved);
//  }
//
//  @GetMapping("/benefits/preview")
//  public ResponseEntity<MembershipBenefitResponse> previewBenefit(
//      @RequestParam String phone,
//      @RequestParam BigDecimal amount,
//      @RequestParam(required = false) String washType
//  ) {
//    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;
//
//    Optional<Membership> memOpt = membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE");
//    if (memOpt.isEmpty()) {
//      return ResponseEntity.ok(buildResponse(null, original, false, BigDecimal.ZERO, original, "No active membership."));
//    }
//
//    Membership m = memOpt.get();
//    if (!isActiveAndValid(m)) {
//      return ResponseEntity.ok(buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active."));
//    }
//
//    if (canConsumeFree(m, washType)) {
//      return ResponseEntity.ok(buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO, "Free booking available."));
//    }
//
//    LocalDateTime now = LocalDateTime.now();
//    if (m.getDiscountStartDate() != null && m.getDiscountEndDate() != null
//        && (now.isEqual(m.getDiscountStartDate()) || now.isAfter(m.getDiscountStartDate()))
//        && now.isBefore(m.getDiscountEndDate())) {
//
//      BigDecimal pct = (m.getDiscountPercent() == null) ? BigDecimal.ZERO : m.getDiscountPercent();
//      BigDecimal discountAmt = original.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//      BigDecimal payable = original.subtract(discountAmt);
//      if (payable.compareTo(BigDecimal.ZERO) < 0) payable = BigDecimal.ZERO;
//
//      return ResponseEntity.ok(buildResponse(m, original, false, pct, payable, "Discount applied: " + pct + "%"));
//    }
//
//    return ResponseEntity.ok(buildResponse(m, original, false, BigDecimal.ZERO, original, "No free left and discount period ended."));
//  }
//
//  @PostMapping("/benefits/apply")
//  @Transactional
//  public ResponseEntity<MembershipBenefitResponse> applyBenefit(
//      @RequestParam String phone,
//      @RequestParam BigDecimal amount,
//      @RequestParam(required = false) String washType,
//      @RequestParam(required = false) String bookingTxnId
//  ) {
//    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;
//
//    Optional<Membership> memOpt = membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE");
//    if (memOpt.isEmpty()) {
//      return ResponseEntity.ok(buildResponse(null, original, false, BigDecimal.ZERO, original, "No active membership."));
//    }
//
//    Membership m = memOpt.get();
//
//    log.info("APPLY called: phone={}, washType={}, membershipDbId={}, freeFoamRemaining={}, freePremiumRemaining={}",
//        phone, washType, m.getId(), m.getFreeFoamRemaining(), m.getFreePremiumRemaining());
//
//    if (!isActiveAndValid(m)) {
//      return ResponseEntity.ok(buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active."));
//    }
//
//    if (canConsumeFree(m, washType)) {
//      consumeFree(m, washType);
//      membershipRepository.saveAndFlush(m);
//
//      return ResponseEntity.ok(buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO,
//          "Free booking consumed successfully."));
//    }
//
//    return previewBenefit(phone, original, washType);
//  }
//
//  @PostMapping("/{membershipDbId}/consume-free")
//  @Transactional
//  public ResponseEntity<MembershipBenefitResponse> consumeFreeDirect(
//      @PathVariable Long membershipDbId,
//      @RequestParam String washType,
//      @RequestParam(required = false) BigDecimal amount,
//      @RequestParam(required = false) String transactionId
//  ) {
//    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;
//
//    Optional<Membership> opt = membershipRepository.findById(membershipDbId);
//    if (opt.isEmpty()) {
//      return ResponseEntity.ok(buildResponse(null, original, false, BigDecimal.ZERO, original, "Membership not found."));
//    }
//
//    Membership m = opt.get();
//
//    if (!isActiveAndValid(m)) {
//      return ResponseEntity.ok(buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active."));
//    }
//
//    if (!canConsumeFree(m, washType)) {
//      return ResponseEntity.ok(buildResponse(m, original, false, BigDecimal.ZERO, original,
//          "No free wash remaining for " + normalizeWashType(washType)));
//    }
//
//    consumeFree(m, washType);
//    membershipRepository.saveAndFlush(m);
//
//    return ResponseEntity.ok(buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO,
//        "Free booking consumed successfully (direct)."));
//  }
//}
