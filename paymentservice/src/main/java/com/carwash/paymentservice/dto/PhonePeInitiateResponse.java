package com.carwash.paymentservice.dto;

public class PhonePeInitiateResponse {

    private String redirectUrl;
    private String merchantTransactionId;

    public PhonePeInitiateResponse() {
    }

    public PhonePeInitiateResponse(String redirectUrl, String merchantTransactionId) {
        this.redirectUrl = redirectUrl;
        this.merchantTransactionId = merchantTransactionId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }
}
