//package com.carwash.paymentservice.controller;
//
//import com.carwash.paymentservice.configuration.PaytmConfig;
//import com.carwash.paymentservice.dto.PaytmInitiateRequest;
//import com.carwash.paymentservice.dto.PaytmInitiateResponse;
//import com.carwash.paymentservice.entity.PaymentOrder;
//import com.carwash.paymentservice.repository.PaymentOrderRepository;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import jakarta.servlet.http.HttpServletRequest;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.*;
//
//@RestController
//@RequestMapping("/payments/paytm")
//@CrossOrigin(origins = "*")
//public class PaytmPaymentController {
//
//    @Autowired
//    private PaytmConfig paytmConfig;
//
//    @Autowired
//    private PaymentOrderRepository paymentOrderRepository;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    /* ========== INITIATE PAYMENT ========== */
//    @PostMapping("/initiate")
//    public ResponseEntity<?> initiate(@RequestBody PaytmInitiateRequest req) throws Exception {
//
//        if (req.getAmount() == null || req.getAmount() <= 0) {
//            return ResponseEntity.badRequest().body("Invalid amount");
//        }
//
//        String orderId = "CW_" + System.currentTimeMillis();
//
//        // 1) Persist pending payment + bookings
//        PaymentOrder po = new PaymentOrder();
//        po.setOrderId(orderId);
//        po.setAmount(req.getAmount());
//        po.setPhone(req.getPhone());
//        po.setCustomerId(
//                req.getCustomerId() != null ? req.getCustomerId() : req.getPhone()
//        );
//        po.setServiceType(req.getServiceType());
//        po.setBookingsJson(objectMapper.writeValueAsString(req.getBookings()));
//        po.setStatus("INITIATED");
//        paymentOrderRepository.save(po);
//
//        // 2) Build Paytm initiateTransaction body
//        JSONObject body = new JSONObject();
//        body.put("requestType", "Payment");
//        body.put("mid", paytmConfig.getMid());
//        body.put("websiteName", paytmConfig.getWebsite());
//        body.put("orderId", orderId);
//        body.put("callbackUrl", paytmConfig.getCallbackUrl());
//
//        JSONObject txnAmount = new JSONObject();
//        txnAmount.put("value", String.format("%.2f", req.getAmount()));
//        txnAmount.put("currency", "INR");
//        body.put("txnAmount", txnAmount);
//
//        JSONObject userInfo = new JSONObject();
//        userInfo.put("custId", po.getCustomerId());
//        if (req.getPhone() != null) userInfo.put("mobile", req.getPhone());
//        body.put("userInfo", userInfo);
//
//        // 3) Generate checksum using your custom PaytmChecksum class
//        String checksum = PaytmChecksum.generateSignature(
//                body.toString(),
//                paytmConfig.getMerchantKey()
//        );
//
//        JSONObject head = new JSONObject();
//        head.put("signature", checksum);
//
//        JSONObject paytmParams = new JSONObject();
//        paytmParams.put("body", body);
//        paytmParams.put("head", head);
//
//        // 4) Call Paytm initiateTransaction API
//        String url = paytmConfig.getTxnUrl()
//                + "?mid=" + paytmConfig.getMid()
//                + "&orderId=" + orderId;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<String> entity = new HttpEntity<>(paytmParams.toString(), headers);
//
//        ResponseEntity<String> resp = restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                entity,
//                String.class
//        );
//
//        if (!resp.getStatusCode().is2xxSuccessful()) {
//            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
//                    .body("Failed to initiate Paytm transaction");
//        }
//
//        JSONObject respJson = new JSONObject(resp.getBody());
//        JSONObject respBody = respJson.getJSONObject("body");
//        JSONObject resultInfo = respBody.getJSONObject("resultInfo");
//
//        String resultStatus = resultInfo.getString("resultStatus");
//        if (!"S".equalsIgnoreCase(resultStatus)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("Paytm error: " + resultInfo.optString("resultMsg"));
//        }
//
//        String txnToken = respBody.getString("txnToken");
//
//        PaytmInitiateResponse out = new PaytmInitiateResponse();
//        out.setMid(paytmConfig.getMid());
//        out.setOrderId(orderId);
//        out.setTxnToken(txnToken);
//        out.setAmount(req.getAmount());
//        out.setCallbackUrl(paytmConfig.getCallbackUrl());
//        out.setEnv(paytmConfig.getEnv());
//
//        return ResponseEntity.ok(out);
//    }
//
//    /* ========== CALLBACK FROM PAYTM ========== */
//    @PostMapping("/callback")
//    public ResponseEntity<String> callback(HttpServletRequest request) throws IOException {
//
//        // Paytm posts form-encoded params OR JSON depending on integration.
//        // We'll read raw body and also check parameters.
//        StringBuilder sb = new StringBuilder();
//        try (BufferedReader reader = request.getReader()) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//        }
//
//        String rawBody = sb.toString();
//        System.out.println("Paytm callback raw body: " + rawBody);
//
//        // Try to parse as JSON first
//        String orderId = null;
//        String status = null;
//
//        if (rawBody != null && rawBody.startsWith("{")) {
//            JSONObject json = new JSONObject(rawBody);
//            orderId = json.optString("ORDERID", null);
//            status = json.optString("STATUS", null);
//        } else {
//            // Fallback: form-encoded params
//            Map<String, String[]> paramMap = request.getParameterMap();
//            if (paramMap.containsKey("ORDERID")) {
//                orderId = paramMap.get("ORDERID")[0];
//            }
//            if (paramMap.containsKey("STATUS")) {
//                status = paramMap.get("STATUS")[0];
//            }
//        }
//
//        if (orderId == null) {
//            return ResponseEntity.badRequest().body("Missing ORDERID in callback");
//        }
//
//        Optional<PaymentOrder> opt = paymentOrderRepository.findByOrderId(orderId);
//        if (opt.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Unknown orderId " + orderId);
//        }
//
//        PaymentOrder po = opt.get();
//
//        // In real-world you should:
//        // 1) verify checksum from callback
//        // 2) hit Paytm Payment Status API to confirm TXN_SUCCESS
//        // For brevity we trust STATUS here.
//
//        if ("TXN_SUCCESS".equalsIgnoreCase(status)) {
//            po.setStatus("SUCCESS");
//            paymentOrderRepository.save(po);
//
//            // Create bookings by calling existing /bookings/bulk
//            try {
//                List<Object> bookings = objectMapper.readValue(
//                        po.getBookingsJson(),
//                        new TypeReference<List<Object>>() {}
//                );
//
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//
//                HttpEntity<String> entity = new HttpEntity<>(
//                        objectMapper.writeValueAsString(bookings),
//                        headers
//                );
//
//                restTemplate.postForEntity(
//                        paytmConfig.getBookingsBulkUrl(),
//                        entity,
//                        String.class
//                );
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body("Payment success but failed to create bookings");
//            }
//
//            // You can redirect to success page if this callback is browser-facing
//            return ResponseEntity.ok("Payment success & bookings created");
//        } else {
//            po.setStatus("FAILED");
//            paymentOrderRepository.save(po);
//            return ResponseEntity.ok("Payment failed / cancelled for order " + orderId);
//        }
//    }
//}
