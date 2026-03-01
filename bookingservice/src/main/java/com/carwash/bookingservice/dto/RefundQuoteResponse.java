package com.carwash.bookingservice.dto;

public class RefundQuoteResponse {

    private boolean eligible;
    private String message;

    private double hoursRemaining;
    private double refundPercent;
    private double bookingAmount;
    private double refundAmount;
	public boolean isEligible() {
		return eligible;
	}
	public void setEligible(boolean eligible) {
		this.eligible = eligible;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public double getHoursRemaining() {
		return hoursRemaining;
	}
	public void setHoursRemaining(double hoursRemaining) {
		this.hoursRemaining = hoursRemaining;
	}
	public double getRefundPercent() {
		return refundPercent;
	}
	public void setRefundPercent(double refundPercent) {
		this.refundPercent = refundPercent;
	}
	public double getBookingAmount() {
		return bookingAmount;
	}
	public void setBookingAmount(double bookingAmount) {
		this.bookingAmount = bookingAmount;
	}
	public double getRefundAmount() {
		return refundAmount;
	}
	public void setRefundAmount(double refundAmount) {
		this.refundAmount = refundAmount;
	}

    
}
