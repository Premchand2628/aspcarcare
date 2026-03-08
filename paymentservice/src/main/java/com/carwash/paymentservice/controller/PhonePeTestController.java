//package com.carwash.paymentservice.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//@RestController
//@RequestMapping("/payments/phonepe")
//public class PhonePeTestController {
//
//    // In-memory store for test only: txnId -> bookingsJson
//    private static final Map<String, String> BOOKINGS_STORE = new ConcurrentHashMap<>();
//
//    private final ObjectMapper objectMapper;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public PhonePeTestController(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    // DTOs
//    public static class PhonePeTestInitiateRequest {
//        private String mobileNumber;
//        private Long amount;     // in rupees
//        private Object bookings; // this will be the list from your review page
//
//        public String getMobileNumber() { return mobileNumber; }
//        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
//        public Long getAmount() { return amount; }
//        public void setAmount(Long amount) { this.amount = amount; }
//        public Object getBookings() { return bookings; }
//        public void setBookings(Object bookings) { this.bookings = bookings; }
//    }
//
//    public static class PhonePeTestInitiateResponse {
//        private String redirectUrl;
//        private String merchantTransactionId;
//
//        public PhonePeTestInitiateResponse(String redirectUrl, String merchantTransactionId) {
//            this.redirectUrl = redirectUrl;
//            this.merchantTransactionId = merchantTransactionId;
//        }
//
//        public String getRedirectUrl() { return redirectUrl; }
//        public String getMerchantTransactionId() { return merchantTransactionId; }
//    }
//
//    public static class PhonePeSimulatorCallbackRequest {
//        private String merchantTransactionId;
//        private String status; // SUCCESS or FAILED
//
//        public String getMerchantTransactionId() { return merchantTransactionId; }
//        public void setMerchantTransactionId(String merchantTransactionId) { this.merchantTransactionId = merchantTransactionId; }
//        public String getStatus() { return status; }
//        public void setStatus(String status) { this.status = status; }
//    }
//
//    @PostMapping("/initiate")
//    public ResponseEntity<?> initiate(@RequestBody PhonePeTestInitiateRequest req) throws Exception {
//
//        if (req.getAmount() == null || req.getAmount() <= 0) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Invalid amount"
//            ));
//        }
//        if (req.getBookings() == null) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "No bookings payload"
//            ));
//        }
//
//        // Create fake transaction
//        String txnId = UUID.randomUUID().toString().replace("-", "");
//
//        String bookingsJson = objectMapper.writeValueAsString(req.getBookings());
//        BOOKINGS_STORE.put(txnId, bookingsJson);
//
//        // phonepe-simulator.html is in /static, served by same app
//        String redirectUrl = "/phonepe-simulator.html?txnId=" + txnId + "&amount=" + req.getAmount();
//
//        PhonePeTestInitiateResponse resp = new PhonePeTestInitiateResponse(redirectUrl, txnId);
//        return ResponseEntity.ok(resp);
//    }
//
//    @PostMapping("/simulate-callback")
//    public ResponseEntity<?> simulateCallback(@RequestBody PhonePeSimulatorCallbackRequest req) {
//        String txnId = req.getMerchantTransactionId();
//        String status = req.getStatus();
//
//        if (txnId == null || txnId.isBlank()) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", "Missing merchantTransactionId"
//            ));
//        }
//
//        String bookingsJson = BOOKINGS_STORE.remove(txnId);
//        if (bookingsJson == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
//                    "success", false,
//                    "message", "Transaction not found or already processed"
//            ));
//        }
//
//        if (!"SUCCESS".equalsIgnoreCase(status)) {
//            // Payment failed – don't create bookings
//            return ResponseEntity.ok(Map.of(
//                    "success", false,
//                    "message", "Payment marked as FAILED"
//            ));
//        }
//
//        try {
//            // Forward to your existing /bookings/bulk endpoint
//            String url = "http://localhost:8082/bookings/bulk"; // adjust if your port/path differs
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<String> entity = new HttpEntity<>(bookingsJson, headers);
//            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
//
//            if (!resp.getStatusCode().is2xxSuccessful()) {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                        "success", false,
//                        "message", "Booking creation failed with status " + resp.getStatusCode().value()
//                ));
//            }
//
//            return ResponseEntity.ok(Map.of(
//                    "success", true,
//                    "message", "Payment success & bookings created",
//                    "bookingResponse", resp.getBody()
//            ));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "success", false,
//                    "message", "Error while creating bookings: " + e.getMessage()
//            ));
//        }
//    }
//}
