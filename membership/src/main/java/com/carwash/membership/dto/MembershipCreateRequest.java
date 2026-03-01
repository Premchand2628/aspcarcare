package com.carwash.membership.dto;

import java.math.BigDecimal;

public class MembershipCreateRequest {

  private String phone;
  private String planCode;       // BASIC / PREMIUM / ULTRA
  private String planName;

  // IMPORTANT:
  // - For NEW: originalPlanPrice == paidAmount == plan price
  // - For UPGRADE: originalPlanPrice = full target plan price (799/1599)
  //              paidAmount        = amount user paid (gap or full)
  private BigDecimal originalPlanPrice;
  private BigDecimal paidAmount;

  private String transactionId;

  // Upgrade info
  private String purchaseMode;           // NEW | UPGRADE_GAP | UPGRADE_FULL
  private Long sourceMembershipDbId;     // old membership table id
  private String previousPlanCode;       // old plan code (BASIC/PREMIUM/ULTRA)
  private BigDecimal upgradeDifferenceAmount; // generally same as paidAmount for GAP upgrades

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getPlanCode() { return planCode; }
  public void setPlanCode(String planCode) { this.planCode = planCode; }

  public String getPlanName() { return planName; }
  public void setPlanName(String planName) { this.planName = planName; }

  public BigDecimal getOriginalPlanPrice() { return originalPlanPrice; }
  public void setOriginalPlanPrice(BigDecimal originalPlanPrice) { this.originalPlanPrice = originalPlanPrice; }

  public BigDecimal getPaidAmount() { return paidAmount; }
  public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

  public String getTransactionId() { return transactionId; }
  public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

  public String getPurchaseMode() { return purchaseMode; }
  public void setPurchaseMode(String purchaseMode) { this.purchaseMode = purchaseMode; }

  public Long getSourceMembershipDbId() { return sourceMembershipDbId; }
  public void setSourceMembershipDbId(Long sourceMembershipDbId) { this.sourceMembershipDbId = sourceMembershipDbId; }

  public String getPreviousPlanCode() { return previousPlanCode; }
  public void setPreviousPlanCode(String previousPlanCode) { this.previousPlanCode = previousPlanCode; }

  public BigDecimal getUpgradeDifferenceAmount() { return upgradeDifferenceAmount; }
  public void setUpgradeDifferenceAmount(BigDecimal upgradeDifferenceAmount) { this.upgradeDifferenceAmount = upgradeDifferenceAmount; }
}
//package com.carwash.membership.dto;
//
//import java.math.BigDecimal;
//
//public class MembershipCreateRequest {
//  private String phone;
//  private String planCode;       // BASIC / PREMIUM / ULTRA
//  private String planName;
//  private BigDecimal price;      // ✅ price user is paying now (gap/full)
//
//  private String transactionId;
//
//  // ✅ NEW for upgrade tracking
//  private String purchaseMode;              // NEW / UPGRADE_FULL / UPGRADE_GAP
//  private Long upgradedFromMembershipId;    // current membership DB id
//  private String previousPlanCode;          // BASIC/PREMIUM/ULTRA
//  private BigDecimal upgradeDifferenceAmount; // gap amount or full amount (what you charged)
//
//  public String getPhone() { return phone; }
//  public void setPhone(String phone) { this.phone = phone; }
//
//  public String getPlanCode() { return planCode; }
//  public void setPlanCode(String planCode) { this.planCode = planCode; }
//
//  public String getPlanName() { return planName; }
//  public void setPlanName(String planName) { this.planName = planName; }
//
//  public BigDecimal getPrice() { return price; }
//  public void setPrice(BigDecimal price) { this.price = price; }
//
//  public String getTransactionId() { return transactionId; }
//  public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
//
//  public String getPurchaseMode() { return purchaseMode; }
//  public void setPurchaseMode(String purchaseMode) { this.purchaseMode = purchaseMode; }
//
//  public Long getUpgradedFromMembershipId() { return upgradedFromMembershipId; }
//  public void setUpgradedFromMembershipId(Long upgradedFromMembershipId) { this.upgradedFromMembershipId = upgradedFromMembershipId; }
//
//  public String getPreviousPlanCode() { return previousPlanCode; }
//  public void setPreviousPlanCode(String previousPlanCode) { this.previousPlanCode = previousPlanCode; }
//
//  public BigDecimal getUpgradeDifferenceAmount() { return upgradeDifferenceAmount; }
//  public void setUpgradeDifferenceAmount(BigDecimal upgradeDifferenceAmount) { this.upgradeDifferenceAmount = upgradeDifferenceAmount; }
//}
