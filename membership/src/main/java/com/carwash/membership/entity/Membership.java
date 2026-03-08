package com.carwash.membership.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // ✅ IMPORTANT
@Entity
@Table(name = "carwash_membership")
public class Membership {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(name = "membership_id", nullable = false, length = 22, unique = true)
  private String membershipId;

  @Column(name = "plan_code", length = 50)
  private String planCode;

  @Column(name = "plan_name", length = 100)
  private String planName;

  @Column
  private BigDecimal price;

  @Column(length = 20)
  private String status; // HOLD/ACTIVE/CANCELLED/EXPIRED

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "discount_percent")
  private BigDecimal discountPercent;

  @Column(name = "transaction_id", length = 80)
  private String transactionId;

  @Column(name = "free_foam_remaining")
  private Integer freeFoamRemaining;

  @Column(name = "free_premium_remaining")
  private Integer freePremiumRemaining;

  @Column(name = "discount_start_date")
  private LocalDateTime discountStartDate;

  @Column(name = "discount_end_date")
  private LocalDateTime discountEndDate;

  // NEW columns (strings)
  @Column(name = "free_used1", length = 30)
  private String freeUsed1;

  @Column(name = "free_used2", length = 30)
  private String freeUsed2;

  @Column(name = "free_used3", length = 30)
  private String freeUsed3;
  @Column(name = "is_upgrade")
  private Boolean isUpgrade = false;

  @Column(name = "upgraded_from_membership_id")
  private Long upgradedFromMembershipId;

  @Column(name = "previous_plan_code")
  private String previousPlanCode;

  @Column(name = "upgrade_difference_amount")
  private BigDecimal upgradeDifferenceAmount;

  @Column(name = "upgrade_created_at")
  private LocalDateTime upgradeCreatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // -------- getters/setters --------

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getMembershipId() { return membershipId; }
  public void setMembershipId(String membershipId) { this.membershipId = membershipId; }

  public String getPlanCode() { return planCode; }
  public void setPlanCode(String planCode) { this.planCode = planCode; }

  public String getPlanName() { return planName; }
  public void setPlanName(String planName) { this.planName = planName; }

  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public LocalDateTime getStartDate() { return startDate; }
  public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

  public LocalDateTime getEndDate() { return endDate; }
  public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

  public BigDecimal getDiscountPercent() { return discountPercent; }
  public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

  public String getTransactionId() { return transactionId; }
  public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

  public Integer getFreeFoamRemaining() { return freeFoamRemaining; }
  public void setFreeFoamRemaining(Integer freeFoamRemaining) { this.freeFoamRemaining = freeFoamRemaining; }

  public Integer getFreePremiumRemaining() { return freePremiumRemaining; }
  public void setFreePremiumRemaining(Integer freePremiumRemaining) { this.freePremiumRemaining = freePremiumRemaining; }

  public LocalDateTime getDiscountStartDate() { return discountStartDate; }
  public void setDiscountStartDate(LocalDateTime discountStartDate) { this.discountStartDate = discountStartDate; }

  public LocalDateTime getDiscountEndDate() { return discountEndDate; }
  public void setDiscountEndDate(LocalDateTime discountEndDate) { this.discountEndDate = discountEndDate; }

  public String getFreeUsed1() { return freeUsed1; }
  public void setFreeUsed1(String freeUsed1) { this.freeUsed1 = freeUsed1; }

  public String getFreeUsed2() { return freeUsed2; }
  public void setFreeUsed2(String freeUsed2) { this.freeUsed2 = freeUsed2; }

  public String getFreeUsed3() { return freeUsed3; }
  public void setFreeUsed3(String freeUsed3) { this.freeUsed3 = freeUsed3; }

public Boolean getIsUpgrade() {
	return isUpgrade;
}

public void setIsUpgrade(Boolean isUpgrade) {
	this.isUpgrade = isUpgrade;
}

public Long getUpgradedFromMembershipId() {
	return upgradedFromMembershipId;
}

public void setUpgradedFromMembershipId(Long upgradedFromMembershipId) {
	this.upgradedFromMembershipId = upgradedFromMembershipId;
}

public String getPreviousPlanCode() {
	return previousPlanCode;
}

public void setPreviousPlanCode(String previousPlanCode) {
	this.previousPlanCode = previousPlanCode;
}

public BigDecimal getUpgradeDifferenceAmount() {
	return upgradeDifferenceAmount;
}

public void setUpgradeDifferenceAmount(BigDecimal upgradeDifferenceAmount) {
	this.upgradeDifferenceAmount = upgradeDifferenceAmount;
}

public LocalDateTime getUpgradeCreatedAt() {
	return upgradeCreatedAt;
}

public void setUpgradeCreatedAt(LocalDateTime upgradeCreatedAt) {
	this.upgradeCreatedAt = upgradeCreatedAt;
}
  
  
}
