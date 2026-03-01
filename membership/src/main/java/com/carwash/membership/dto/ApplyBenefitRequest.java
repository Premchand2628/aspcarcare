package com.carwash.membership.dto;

import java.math.BigDecimal;

public class ApplyBenefitRequest {
  private String phone;
  private String washPackage; // FOAM / PREMIUM / NORMAL
  private BigDecimal amount;

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getWashPackage() { return washPackage; }
  public void setWashPackage(String washPackage) { this.washPackage = washPackage; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
}
