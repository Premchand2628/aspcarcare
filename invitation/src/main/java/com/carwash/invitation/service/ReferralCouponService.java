package com.carwash.invitation.service;

import com.carwash.invitation.dto.*;
import com.carwash.invitation.entity.ReferralCoupon;
import com.carwash.invitation.entity.ReferralCouponRedemption;
import com.carwash.invitation.repository.ReferralCouponRedemptionRepository;
import com.carwash.invitation.repository.ReferralCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class ReferralCouponService {

    private final ReferralCouponRepository couponRepo;
    private final ReferralCouponRedemptionRepository redemptionRepo;

    public ReferralCouponService(ReferralCouponRepository couponRepo,
                                 ReferralCouponRedemptionRepository redemptionRepo) {
        this.couponRepo = couponRepo;
        this.redemptionRepo = redemptionRepo;
    }

    // -------- Generate Coupon --------
    @Transactional
    public GenerateCouponResponse generate(GenerateCouponRequest req) {
        String discountType = normalizeDiscountType(req.getDiscountType());

        int maxUses = (req.getMaxUses() == null || req.getMaxUses() < 1) ? 1 : req.getMaxUses();
        int validDays = (req.getValidDays() == null || req.getValidDays() < 1) ? 30 : req.getValidDays();

        String code = generateUniqueCode("ASP", 8);

        LocalDateTime now = LocalDateTime.now();

        ReferralCoupon c = ReferralCoupon.builder()
                .couponCode(code)
                .createdByPhone(req.getCreatedByPhone())
                .discountType(discountType)
                .discountValue(req.getDiscountValue())
                .maxUses(maxUses)
                .validFrom(now)
                .validUntil(now.plusDays(validDays))
                .minOrderAmount(req.getMinOrderAmount())
                .maxDiscountAmount(req.getMaxDiscountAmount())
                .status("ACTIVE")
                // if you have usedCount default:
                .usedCount(0)
                .build();

        couponRepo.save(c);

        String shareText =
                "Hey! 👋 I’m using ASP Car Care 🚗💦\n\n" +
                "Use my coupon code *" + code + "* to get discount 🎉\n" +
                "Book here: " + "(your-site-link)" + "\n" +
                "(Apply coupon at checkout)";

        return GenerateCouponResponse.builder()
                .couponCode(code)
                .shareText(shareText)
                .build();
    }

    // -------- Validate Coupon (before checkout) --------
    public ValidateCouponResponse validate(ValidateCouponRequest req) {
        String code = safeUpper(req.getCouponCode());
        ReferralCoupon c = couponRepo.findByCouponCode(code).orElse(null);

        if (c == null) return invalid("Invalid coupon code");
        if (!"ACTIVE".equalsIgnoreCase(c.getStatus())) return invalid("Coupon is not active");
        if (c.getValidUntil() != null && LocalDateTime.now().isAfter(c.getValidUntil()))
            return invalid("Coupon expired");
        if (c.getUsedCount() != null && c.getMaxUses() != null && c.getUsedCount() >= c.getMaxUses())
            return invalid("Coupon limit reached");

        // Prevent self-use
        if (normalizePhone(req.getUserPhone()).equals(normalizePhone(c.getCreatedByPhone())))
            return invalid("You cannot use your own referral coupon");

        // Optional: “one coupon per user only”
        if (redemptionRepo.existsByCouponCodeAndUsedByPhone(code, normalizePhone(req.getUserPhone())))
            return invalid("You already used this coupon");

        BigDecimal orderAmount = req.getOrderAmount() == null ? BigDecimal.ZERO : req.getOrderAmount();

        if (c.getMinOrderAmount() != null && orderAmount.compareTo(c.getMinOrderAmount()) < 0)
            return invalid("Minimum order amount required: " + c.getMinOrderAmount());

        BigDecimal discount = computeDiscount(c, orderAmount);
        BigDecimal finalPay = orderAmount.subtract(discount).max(BigDecimal.ZERO);

        return ValidateCouponResponse.builder()
                .valid(true)
                .message("Coupon applied")
                .discountAmount(discount)
                .finalPayAmount(finalPay)
                .build();
    }

    // -------- Redeem Coupon (after payment success / booking created) --------
    @Transactional
    public void redeem(RedeemCouponRequest req) {
        String code = safeUpper(req.getCouponCode());

        ReferralCoupon c = couponRepo.findByCouponCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code"));

        // Re-validate quickly (avoid race conditions)
        ValidateCouponRequest vreq = ValidateCouponRequest.builder()
                .couponCode(code)
                .userPhone(req.getUserPhone())
                .orderAmount(req.getOrderAmount())
                .build();

        ValidateCouponResponse vres = validate(vreq);
        if (!Boolean.TRUE.equals(vres.isValid())) {
            throw new IllegalStateException(vres.getMessage());
        }

        // Record redemption
        ReferralCouponRedemption red = ReferralCouponRedemption.builder()
                .couponId(c.getId())
                .couponCode(code)
                .usedByPhone(normalizePhone(req.getUserPhone()))
                .bookingId(req.getBookingId())
                .transactionId(req.getTransactionId())
                .orderAmount(req.getOrderAmount())
                .discountAmount(vres.getDiscountAmount())
                .build();

        redemptionRepo.save(red);

        // Increment used_count
        int currentUsed = (c.getUsedCount() == null ? 0 : c.getUsedCount());
        int newUsed = currentUsed + 1;

        // If reached max, mark inactive
        String newStatus = c.getStatus();
        if (c.getMaxUses() != null && newUsed >= c.getMaxUses()) {
            newStatus = "INACTIVE";
        }

        // Update coupon using toBuilder (no setters)
        ReferralCoupon updated = c.toBuilder()
                .usedCount(newUsed)
                .status(newStatus)
                .build();

        couponRepo.save(updated);
    }

        public Map<String, Object> referralDetails(String userPhone) {
        String normalizedPhone = normalizePhone(userPhone);
        List<ReferralCoupon> coupons = couponRepo.findByCreatedByPhone(normalizedPhone);

        if (coupons.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("referrals", List.of());
            empty.put("totalBenefit", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return empty;
        }

        List<String> couponCodes = coupons.stream()
            .map(ReferralCoupon::getCouponCode)
            .filter(code -> code != null && !code.isBlank())
            .toList();

        List<ReferralCouponRedemption> redemptions = couponCodes.isEmpty()
            ? List.of()
            : redemptionRepo.findByCouponCodeIn(couponCodes);

        List<Map<String, Object>> referrals = redemptions.stream()
            .sorted(Comparator.comparing(ReferralCouponRedemption::getRedeemedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(redemption -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", redemption.getId());
                item.put("referredPhone", redemption.getUsedByPhone());
                item.put("referredName", redemption.getUsedByPhone());
                item.put("referralDate",
                    redemption.getRedeemedAt() != null ? redemption.getRedeemedAt().toLocalDate().toString() : null);
                item.put("benefitAmount",
                    redemption.getDiscountAmount() != null ? redemption.getDiscountAmount() : BigDecimal.ZERO);
                item.put("status", "completed");
                return item;
            })
            .collect(Collectors.toList());

        BigDecimal totalBenefit = redemptions.stream()
            .map(ReferralCouponRedemption::getDiscountAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> response = new HashMap<>();
        response.put("referrals", referrals);
        response.put("totalBenefit", totalBenefit);
        return response;
        }

    // -------- Helpers --------

    private BigDecimal computeDiscount(ReferralCoupon c, BigDecimal orderAmount) {
        BigDecimal discount;

        if ("PERCENT".equalsIgnoreCase(c.getDiscountType())) {
            discount = orderAmount
                    .multiply(c.getDiscountValue())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            if (c.getMaxDiscountAmount() != null && discount.compareTo(c.getMaxDiscountAmount()) > 0) {
                discount = c.getMaxDiscountAmount();
            }
        } else {
            discount = c.getDiscountValue();
        }

        if (discount.compareTo(orderAmount) > 0) discount = orderAmount;
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private ValidateCouponResponse invalid(String msg) {
        return ValidateCouponResponse.builder()
                .valid(false)
                .message(msg)
                .discountAmount(BigDecimal.ZERO)
                .finalPayAmount(null)
                .build();
    }

    private String generateUniqueCode(String prefix, int len) {
        Random r = new Random();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int tries = 0; tries < 50; tries++) {
            StringBuilder sb = new StringBuilder(prefix).append("-");
            for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
            String code = sb.toString();
            if (!couponRepo.existsByCouponCode(code)) return code;
        }
        throw new IllegalStateException("Unable to generate coupon code");
    }

    private String normalizeDiscountType(String t) {
        String x = (t == null ? "" : t.trim().toUpperCase(Locale.ROOT));
        if (!x.equals("PERCENT") && !x.equals("FLAT")) return "PERCENT";
        return x;
    }

    private String safeUpper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePhone(String p) {
        if (p == null) return "";
        return p.replaceAll("[^0-9+]", "");
    }
}

