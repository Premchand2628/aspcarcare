//package com.carwash.paymentservice.configuration;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PaytmConfig {
//
//    @Value("${paytm.mid}")
//    private String mid;
//
//    @Value("${paytm.merchantKey}")
//    private String merchantKey;
//
//    @Value("${paytm.website}")
//    private String website;
//
//    @Value("${paytm.callbackUrl}")
//    private String callbackUrl;
//
//    @Value("${paytm.txn.url}")
//    private String txnUrl;
//
//    @Value("${paytm.env}")
//    private String env; // STAGE or PROD
//
//    @Value("${carwash.baseUrl}")
//    private String carwashBaseUrl;
//
//    @Value("${carwash.bookings.bulkPath}")
//    private String bookingsBulkPath;
//
//    public String getMid() { return mid; }
//    public String getMerchantKey() { return merchantKey; }
//    public String getWebsite() { return website; }
//    public String getCallbackUrl() { return callbackUrl; }
//    public String getTxnUrl() { return txnUrl; }
//    public String getEnv() { return env; }
//
//    public String getBookingsBulkUrl() {
//        return carwashBaseUrl + bookingsBulkPath;
//    }
//}
