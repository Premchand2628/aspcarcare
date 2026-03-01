package com.carwash.paymentservice.dto;

import java.util.List;

public class PhonePeInitiateRequest {

    private String mobileNumber;
    private Long amount;          // in rupees from frontend
    private List<Object> bookings; // or use your real BookingRequest DTO

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public List<Object> getBookings() {
        return bookings;
    }

    public void setBookings(List<Object> bookings) {
        this.bookings = bookings;
    }
}
