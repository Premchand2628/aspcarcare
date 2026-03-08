package com.carwash.membership.dto;

import java.math.BigDecimal;

public class MembershipBenefitResponse {

  private boolean eligible;
  private boolean freeApplied;

  private BigDecimal discountPercent;
  private BigDecimal originalAmount;
  private BigDecimal payableAmount;

  private String message;

  // For UI display
  private String membershipId;   // 22-char membership_id
  private String planCode;

  // IMPORTANT for booking table (BIGINT)
  private Long membershipDbId;

  public boolean isEligible() { return eligible; }
  public void setEligible(boolean eligible) { this.eligible = eligible; }

  public boolean isFreeApplied() { return freeApplied; }
  public void setFreeApplied(boolean freeApplied) { this.freeApplied = freeApplied; }

  public BigDecimal getDiscountPercent() { return discountPercent; }
  public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

  public BigDecimal getOriginalAmount() { return originalAmount; }
  public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }

  public BigDecimal getPayableAmount() { return payableAmount; }
  public void setPayableAmount(BigDecimal payableAmount) { this.payableAmount = payableAmount; }

  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }

  public String getMembershipId() { return membershipId; }
  public void setMembershipId(String membershipId) { this.membershipId = membershipId; }

  public String getPlanCode() { return planCode; }
  public void setPlanCode(String planCode) { this.planCode = planCode; }

  public Long getMembershipDbId() { return membershipDbId; }
  public void setMembershipDbId(Long membershipDbId) { this.membershipDbId = membershipDbId; }
}
