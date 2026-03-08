package com.carwash.paymentservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhonePeConfig {

    @Value("${phonepe.environment:UAT}")
    private String environment;

    @Value("${phonepe.merchant.id}")
    private String merchantId;

    @Value("${phonepe.salt.key}")
    private String saltKey;

    @Value("${phonepe.salt.index}")
    private String saltIndex;

    @Value("${phonepe.base.url}")
    private String baseUrl;

    @Value("${phonepe.callback.url}")
    private String callbackUrl;

    @Value("${phonepe.client.id:}")
    private String clientId;

    @Value("${phonepe.client.secret:}")
    private String clientSecret;

    public String getEnvironment() {
        return environment;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getSaltKey() {
        return saltKey;
    }

    public String getSaltIndex() {
        return saltIndex;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
