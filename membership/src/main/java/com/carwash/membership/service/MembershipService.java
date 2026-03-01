package com.carwash.membership.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carwash.membership.dto.MembershipBenefitResponse;
import com.carwash.membership.dto.MembershipCreateRequest;
import com.carwash.membership.entity.Membership;
import com.carwash.membership.repository.MembershipRepository;

@Service
public class MembershipService {

  private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

  private final MembershipRepository membershipRepository;

  private final SecureRandom random = new SecureRandom();
  private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  public MembershipService(MembershipRepository membershipRepository) {
    this.membershipRepository = membershipRepository;
  }

  // =========================
  // READ
  // =========================

  public Optional<Membership> getLatestByPhone(String phone) {
    if (phone == null || phone.isBlank()) return Optional.empty();
    MDC.put("event", "business_start");
    return membershipRepository.findTopByPhoneOrderByUpdatedAtDesc(phone.trim());
  }

  public Optional<Membership> getLatestActiveByPhone(String phone) {
    if (phone == null || phone.isBlank()) return Optional.empty();
    MDC.put("event", "business_start");
    return membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE");
  }

  // =========================
  // CREATE / UPDATE
  // =========================

  public Membership createMembership(MembershipCreateRequest request) {
    MDC.put("event", "business_start");

    if (request == null || request.getPhone() == null || request.getPhone().isBlank()) {
      throw new IllegalArgumentException("Phone is required");
    }

    final String phone = request.getPhone().trim();
    final String txnId = (request.getTransactionId() == null) ? null : request.getTransactionId().trim();

    final String planCode = (request.getPlanCode() == null) ? "" : request.getPlanCode().trim().toUpperCase();
    final String planName = (request.getPlanName() == null) ? "" : request.getPlanName().trim();

    if (planCode.isBlank() || planName.isBlank()) {
      throw new IllegalArgumentException("Plan code & plan name are required");
    }

    BigDecimal priceToStore = request.getOriginalPlanPrice();
    if (priceToStore == null) priceToStore = BigDecimal.ZERO;

    BigDecimal paidAmount = request.getPaidAmount();
    if (paidAmount == null) paidAmount = priceToStore;

    String mode = (request.getPurchaseMode() == null) ? "NEW" : request.getPurchaseMode().trim().toUpperCase();
    boolean isUpgrade = mode.startsWith("UPGRADE");

    Membership m = new Membership();
    m.setPhone(phone);
    m.setMembershipId(generateUniqueMembershipId());
    m.setPlanCode(planCode);
    m.setPlanName(planName);

    m.setPrice(priceToStore);
    if (txnId != null && !txnId.isBlank()) m.setTransactionId(txnId);

    // ✅ Always HOLD after payment
    m.setStatus("HOLD");

    LocalDateTime now = LocalDateTime.now();
    m.setStartDate(now);

    int endMonths = membershipEndMonths(planCode);
    m.setEndDate(endMonths > 0 ? now.plusMonths(endMonths) : null);

    m.setDiscountPercent(planDiscount(planCode));
    m.setDiscountStartDate(now);

    int dm = discountMonths(planCode);
    m.setDiscountEndDate(dm > 0 ? now.plusMonths(dm) : null);

    initFreebiesAndResetUsage(m);

    // ✅ Upgrade fields
    if (isUpgrade) {
      m.setIsUpgrade(true);
      m.setUpgradeCreatedAt(now);

      Long sourceDbId = request.getSourceMembershipDbId();
      String prevPlan = request.getPreviousPlanCode();

      Membership source = null;
      if (sourceDbId != null) {
        source = membershipRepository.findById(sourceDbId).orElse(null);
      }
      if (source == null) {
        source = membershipRepository.findTopByPhoneOrderByUpdatedAtDesc(phone).orElse(null);
      }

      if (source != null) {
        m.setUpgradedFromMembershipId(source.getId());
        if (prevPlan == null || prevPlan.isBlank()) prevPlan = source.getPlanCode();
      }

      if (prevPlan != null) m.setPreviousPlanCode(prevPlan.trim().toUpperCase());

      BigDecimal diff = request.getUpgradeDifferenceAmount();
      if (diff == null) diff = paidAmount;
      m.setUpgradeDifferenceAmount(diff);
    } else {
      m.setIsUpgrade(false);
    }

    Membership saved = membershipRepository.save(m);

    MDC.put("membershipDbId", String.valueOf(saved.getId()));
    MDC.put("status", saved.getStatus());
    MDC.put("event", "business_end");
    log.info("Membership created");

    return saved;
  }

  public Membership updateStatus(String membershipId, String value) {
    MDC.put("event", "business_start");

    Membership m = membershipRepository.findByMembershipId(membershipId)
        .orElseThrow(() -> new NoSuchElementException("Membership not found"));

    String newStatus = (value == null ? "" : value.trim().toUpperCase());
    if (!(newStatus.equals("HOLD") || newStatus.equals("ACTIVE") || newStatus.equals("CANCELLED") || newStatus.equals("EXPIRED"))) {
      throw new IllegalArgumentException("Invalid status. Allowed: HOLD, ACTIVE, CANCELLED, EXPIRED");
    }

    m.setStatus(newStatus);
    Membership saved = membershipRepository.save(m);

    MDC.put("membershipDbId", String.valueOf(saved.getId()));
    MDC.put("status", saved.getStatus());
    MDC.put("event", "business_end");
    log.info("Membership status updated");

    return saved;
  }

  // =========================
  // BENEFITS
  // =========================

  public MembershipBenefitResponse previewBenefit(String phone, BigDecimal amount, String washType) {
    MDC.put("event", "business_start");

    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;

    Optional<Membership> memOpt =
        membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE");

    if (memOpt.isEmpty()) {
      return buildResponse(null, original, false, BigDecimal.ZERO, original, "No active membership.");
    }

    Membership m = memOpt.get();
    if (!isActiveAndValid(m)) {
      return buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active.");
    }

    if (canConsumeFree(m, washType)) {
      return buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO, "Free booking available.");
    }

    LocalDateTime now = LocalDateTime.now();
    if (m.getDiscountStartDate() != null && m.getDiscountEndDate() != null
        && (now.isEqual(m.getDiscountStartDate()) || now.isAfter(m.getDiscountStartDate()))
        && now.isBefore(m.getDiscountEndDate())) {

      BigDecimal pct = (m.getDiscountPercent() == null) ? BigDecimal.ZERO : m.getDiscountPercent();
      BigDecimal discountAmt = original.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
      BigDecimal payable = original.subtract(discountAmt);
      if (payable.compareTo(BigDecimal.ZERO) < 0) payable = BigDecimal.ZERO;

      return buildResponse(m, original, false, pct, payable, "Discount applied: " + pct + "%");
    }

    return buildResponse(m, original, false, BigDecimal.ZERO, original, "No free left and discount period ended.");
  }

  @Transactional
  public MembershipBenefitResponse applyBenefit(String phone, BigDecimal amount, String washType, String bookingTxnId) {
    MDC.put("event", "business_start");

    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;

    Optional<Membership> memOpt =
        membershipRepository.findTopByPhoneAndStatusOrderByUpdatedAtDesc(phone.trim(), "ACTIVE");

    if (memOpt.isEmpty()) {
      return buildResponse(null, original, false, BigDecimal.ZERO, original, "No active membership.");
    }

    Membership m = memOpt.get();

    // ✅ bookingTxnId is optional but helpful for debugging
    if (bookingTxnId != null && !bookingTxnId.isBlank()) MDC.put("txnId", bookingTxnId.trim());

    log.info("APPLY called: phone={}, washType={}, membershipDbId={}, freeFoamRemaining={}, freePremiumRemaining={}",
        phone, washType, m.getId(), m.getFreeFoamRemaining(), m.getFreePremiumRemaining());

    if (!isActiveAndValid(m)) {
      return buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active.");
    }

    if (canConsumeFree(m, washType)) {
      consumeFree(m, washType);
      membershipRepository.saveAndFlush(m);
      return buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO, "Free booking consumed successfully.");
    }

    // fallback to preview (discount etc.)
    return previewBenefit(phone, original, washType);
  }

  @Transactional
  public MembershipBenefitResponse consumeFreeDirect(Long membershipDbId, String washType, BigDecimal amount, String transactionId) {
    MDC.put("event", "business_start");

    BigDecimal original = (amount == null) ? BigDecimal.ZERO : amount;

    Membership m = membershipRepository.findById(membershipDbId)
        .orElse(null);

    if (m == null) {
      return buildResponse(null, original, false, BigDecimal.ZERO, original, "Membership not found.");
    }

    if (transactionId != null && !transactionId.isBlank()) MDC.put("txnId", transactionId.trim());

    if (!isActiveAndValid(m)) {
      return buildResponse(m, original, false, BigDecimal.ZERO, original, "Membership expired or not active.");
    }

    if (!canConsumeFree(m, washType)) {
      return buildResponse(m, original, false, BigDecimal.ZERO, original,
          "No free wash remaining for " + normalizeWashType(washType));
    }

    consumeFree(m, washType);
    membershipRepository.saveAndFlush(m);

    return buildResponse(m, original, true, BigDecimal.ZERO, BigDecimal.ZERO,
        "Free booking consumed successfully (direct).");
  }

  // =========================
  // INTERNAL HELPERS (same logic you had)
  // =========================

  private String generateMembershipId() {
    StringBuilder sb = new StringBuilder(22);
    for (int i = 0; i < 22; i++) {
      sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }

  private String generateUniqueMembershipId() {
    String id;
    do { id = generateMembershipId(); }
    while (membershipRepository.existsByMembershipId(id));
    return id;
  }

  private int membershipEndMonths(String planCode) {
    if (planCode == null) return 0;
    switch (planCode.trim().toUpperCase()) {
      case "BASIC": return 3;
      case "PREMIUM": return 6;
      case "ULTRA": return 6;
      default: return 0;
    }
  }

  private int discountMonths(String planCode) {
    if (planCode == null) return 0;
    switch (planCode.trim().toUpperCase()) {
      case "BASIC": return 2;
      case "PREMIUM": return 3;
      case "ULTRA": return 6;
      default: return 0;
    }
  }

  private BigDecimal planDiscount(String planCode) {
    if (planCode == null) return BigDecimal.ZERO;
    switch (planCode.trim().toUpperCase()) {
      case "BASIC": return new BigDecimal("10");
      case "PREMIUM": return new BigDecimal("10");
      case "ULTRA": return new BigDecimal("15");
      default: return BigDecimal.ZERO;
    }
  }

  private void initFreebiesAndResetUsage(Membership m) {
    String p = (m.getPlanCode() == null) ? "" : m.getPlanCode().trim().toUpperCase();

    m.setFreeFoamRemaining(0);
    m.setFreePremiumRemaining(0);

    if ("BASIC".equals(p)) {
      m.setFreeFoamRemaining(1);
      m.setFreePremiumRemaining(0);
    } else if ("PREMIUM".equals(p)) {
      m.setFreeFoamRemaining(2);
      m.setFreePremiumRemaining(0);
    } else if ("ULTRA".equals(p)) {
      m.setFreeFoamRemaining(2);
      m.setFreePremiumRemaining(1);
    }

    m.setFreeUsed1(null);
    m.setFreeUsed2(null);
    m.setFreeUsed3(null);
  }

  private boolean isActiveAndValid(Membership m) {
    if (m == null) return false;
    if (!"ACTIVE".equalsIgnoreCase(m.getStatus())) return false;
    if (m.getEndDate() == null) return false;
    return LocalDateTime.now().isBefore(m.getEndDate());
  }

  private String normalizeWashType(String washType) {
    if (washType == null || washType.isBlank()) return "BASIC";
    String wt = washType.trim().toUpperCase();
    if (wt.contains("PREMIUM")) return "PREMIUM";
    if (wt.contains("FOAM")) return "FOAM";
    return "BASIC";
  }

  private boolean canConsumeFree(Membership m, String washType) {
    String wt = normalizeWashType(washType);
    if ("FOAM".equals(wt)) return m.getFreeFoamRemaining() != null && m.getFreeFoamRemaining() > 0;
    if ("PREMIUM".equals(wt)) return m.getFreePremiumRemaining() != null && m.getFreePremiumRemaining() > 0;
    return false;
  }

  private void recordFreeUse(Membership m, String usedType) {
    if (m.getFreeUsed1() == null) m.setFreeUsed1(usedType);
    else if (m.getFreeUsed2() == null) m.setFreeUsed2(usedType);
    else if (m.getFreeUsed3() == null) m.setFreeUsed3(usedType);
  }

  private void consumeFree(Membership m, String washType) {
    String wt = normalizeWashType(washType);
    if ("FOAM".equals(wt)) {
      m.setFreeFoamRemaining(Math.max(0, (m.getFreeFoamRemaining() == null ? 0 : m.getFreeFoamRemaining()) - 1));
      recordFreeUse(m, "FOAM");
    } else if ("PREMIUM".equals(wt)) {
      m.setFreePremiumRemaining(Math.max(0, (m.getFreePremiumRemaining() == null ? 0 : m.getFreePremiumRemaining()) - 1));
      recordFreeUse(m, "PREMIUM");
    }
  }

  private MembershipBenefitResponse buildResponse(Membership m,
                                                 BigDecimal amount,
                                                 boolean freeApplied,
                                                 BigDecimal discountPct,
                                                 BigDecimal payable,
                                                 String msg) {
    MembershipBenefitResponse r = new MembershipBenefitResponse();
    r.setEligible(m != null && isActiveAndValid(m));
    r.setFreeApplied(freeApplied);
    r.setDiscountPercent(discountPct);
    r.setOriginalAmount(amount);
    r.setPayableAmount(payable);
    r.setMessage(msg);

    if (m != null) {
      r.setMembershipDbId(m.getId());
      r.setMembershipId(m.getMembershipId());
      r.setPlanCode(m.getPlanCode());
    }
    return r;
  }
}
