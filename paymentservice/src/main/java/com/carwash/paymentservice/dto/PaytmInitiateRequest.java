package com.carwash.paymentservice.dto;

import java.util.List;

public class PaytmInitiateRequest {

    private Double amount;
    private String phone;        // customer phone
    private String customerId;   // any ID you want, e.g. phone
    private String serviceType;  // "HOME" / "SELF_DRIVE"
    private List<Object> bookings; // raw booking maps from FE

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public List<Object> getBookings() { return bookings; }
    public void setBookings(List<Object> bookings) { this.bookings = bookings; }
}
