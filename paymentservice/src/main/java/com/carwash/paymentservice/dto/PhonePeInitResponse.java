package com.carwash.paymentservice.dto;

public class PhonePeInitResponse {

    private String success;
    private String code;
    private Data data;

    public static class Data {
        private String merchantId;
        private String merchantTransactionId;
        private String instrumentResponseType;
        private String redirectUrl;

        public String getMerchantId() {
            return merchantId;
        }

        public void setMerchantId(String merchantId) {
            this.merchantId = merchantId;
        }

        public String getMerchantTransactionId() {
            return merchantTransactionId;
        }

        public void setMerchantTransactionId(String merchantTransactionId) {
            this.merchantTransactionId = merchantTransactionId;
        }

        public String getInstrumentResponseType() {
            return instrumentResponseType;
        }

        public void setInstrumentResponseType(String instrumentResponseType) {
            this.instrumentResponseType = instrumentResponseType;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
