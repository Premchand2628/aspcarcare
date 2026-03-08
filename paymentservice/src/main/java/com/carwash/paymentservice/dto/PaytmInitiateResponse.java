package com.carwash.paymentservice.dto;

public class PaytmInitiateResponse {

    private String mid;
    private String orderId;
    private String txnToken;
    private Double amount;
    private String callbackUrl;
    private String env; // STAGE / PROD

    // getters & setters

    public String getMid() { return mid; }
    public void setMid(String mid) { this.mid = mid; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getTxnToken() { return txnToken; }
    public void setTxnToken(String txnToken) { this.txnToken = txnToken; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }
}
