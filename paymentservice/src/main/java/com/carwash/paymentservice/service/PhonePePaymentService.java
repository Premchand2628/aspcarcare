package com.carwash.paymentservice.service;

import com.carwash.paymentservice.configuration.PhonePeConfig;
import com.carwash.paymentservice.dto.PhonePeInitResponse;
import com.carwash.paymentservice.dto.PhonePeInitiateRequest;
import com.carwash.paymentservice.dto.PhonePeInitiateResponse;
import com.carwash.paymentservice.dto.PhonePePayRequest;
import com.carwash.paymentservice.entity.PendingPayment;
import com.carwash.paymentservice.repository.PendingPaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

@Service
public class PhonePePaymentService {

    private final PhonePeConfig config;
    private final PendingPaymentRepository pendingPaymentRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public PhonePePaymentService(PhonePeConfig config,
                                 PendingPaymentRepository pendingPaymentRepository,
                                 ObjectMapper objectMapper) {
        this.config = config;
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.objectMapper = objectMapper;
    }

    public PhonePeInitiateResponse initiatePayment(PhonePeInitiateRequest req) throws Exception {
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (req.getBookings() == null || req.getBookings().isEmpty()) {
            throw new IllegalArgumentException("No bookings payload");
        }

        String bookingsJson = objectMapper.writeValueAsString(req.getBookings());

        // Generate IDs
        String merchantTransactionId = "ASP_" + UUID.randomUUID()
                .toString().replace("-", "").substring(0, 16);
        String merchantOrderId = "ORD_" + UUID.randomUUID()
                .toString().replace("-", "").substring(0, 16);

        // Save in PendingPayment
        PendingPayment pending = new PendingPayment();
        pending.setMerchantTransactionId(merchantTransactionId);
        pending.setMerchantOrderId(merchantOrderId);
        pending.setMobileNumber(req.getMobileNumber());
        pending.setAmountPaise(req.getAmount() * 100); // rupees → paise
        pending.setBookingsJson(bookingsJson);
        pending.setStatus("PENDING");
        pending.setCreatedAt(LocalDateTime.now());
        pending.setUpdatedAt(LocalDateTime.now());
        pendingPaymentRepository.save(pending);

        // 🔴 DEV MODE: if merchant id starts with DUMMY, skip real PhonePe and go to simulator
        boolean isDummyMerchant = config.getMerchantId() != null
                && config.getMerchantId().startsWith("DUMMY");

        if (isDummyMerchant) {
            String redirectUrl = "http://localhost:8080/phonepe-simulator.html"
                    + "?txnId=" + merchantTransactionId
                    + "&amount=" + req.getAmount();

            return new PhonePeInitiateResponse(redirectUrl, merchantTransactionId);
        }

        // 🔵 REAL PhonePe PG flow (for later, when you have real creds)
        PhonePePayRequest payRequest = new PhonePePayRequest();
        payRequest.setMerchantId(config.getMerchantId());
        payRequest.setMerchantTransactionId(merchantTransactionId);
        payRequest.setMerchantUserId(req.getMobileNumber());
        payRequest.setAmount(req.getAmount() * 100);
        payRequest.setCallbackUrl(config.getCallbackUrl());
        payRequest.setRedirectUrl(config.getCallbackUrl());
        payRequest.setMobileNumber(req.getMobileNumber());

        PhonePePayRequest.PaymentInstrument pi = new PhonePePayRequest.PaymentInstrument();
        payRequest.setPaymentInstrument(pi);

        String json = objectMapper.writeValueAsString(payRequest);
        String base64Payload = Base64.getEncoder()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        String apiPath = "/pg/v1/pay"; // adjust when you get official docs
        String stringToSign = base64Payload + apiPath + config.getSaltKey();
        String sha256 = sha256Hex(stringToSign);
        String xVerify = sha256 + "###" + config.getSaltIndex();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-VERIFY", xVerify);
        headers.set("X-MERCHANT-ID", config.getMerchantId());

        // if PhonePe requires client auth, uncomment:
        // if (!config.getClientId().isEmpty()) {
        //     String basic = Base64.getEncoder()
        //         .encodeToString((config.getClientId() + ":" + config.getClientSecret())
        //                 .getBytes(StandardCharsets.UTF_8));
        //     headers.set("Authorization", "Basic " + basic);
        // }

        HashMap<String, Object> body = new HashMap<>();
        body.put("request", base64Payload);

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        String url = config.getBaseUrl() + apiPath;

        ResponseEntity<PhonePeInitResponse> response =
                restTemplate.postForEntity(url, entity, PhonePeInitResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().getData() == null
                || response.getBody().getData().getRedirectUrl() == null) {

            throw new IllegalStateException("Unable to initiate PhonePe payment. Code: "
                    + (response.getBody() != null ? response.getBody().getCode() : "null"));
        }

        String redirectUrl = response.getBody().getData().getRedirectUrl();
        return new PhonePeInitiateResponse(redirectUrl, merchantTransactionId);
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
