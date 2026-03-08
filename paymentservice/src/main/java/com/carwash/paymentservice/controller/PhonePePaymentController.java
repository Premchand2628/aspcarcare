package com.carwash.paymentservice.controller;

import com.carwash.paymentservice.dto.PhonePeInitiateRequest;
import com.carwash.paymentservice.dto.PhonePeInitiateResponse;
import com.carwash.paymentservice.entity.PendingPayment;
import com.carwash.paymentservice.repository.PendingPaymentRepository;
import com.carwash.paymentservice.service.PhonePePaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")   // allow calls from 8080 simulator & 8082 UI
@RestController
@RequestMapping("/payments/phonepe")
public class PhonePePaymentController {

    private static final Logger log = LoggerFactory.getLogger(PhonePePaymentController.class);
    private static final String SERVICE = "paymentservice";

    private final PhonePePaymentService phonePePaymentService;
    private final PendingPaymentRepository pendingRepo;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public PhonePePaymentController(PhonePePaymentService phonePePaymentService,
                                    PendingPaymentRepository pendingRepo,
                                    ObjectMapper objectMapper) {
        this.phonePePaymentService = phonePePaymentService;
        this.pendingRepo = pendingRepo;
        this.objectMapper = objectMapper;
    }

    // ==========================================================
    // Small helpers (only for logging)
    // ==========================================================
    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static String safeStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String shortTxn(String txn) {
        if (txn == null) return null;
        String t = txn.trim();
        if (t.length() <= 6) return t;
        return t.substring(0, 3) + "..." + t.substring(t.length() - 3);
    }

    private static void putTxnId(String txnId) {
        if (txnId != null && !txnId.isBlank()) {
            MDC.put("txnId", txnId.trim()); // standard key
        }
    }

    // --------- INITIATE (already working) ---------
    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestBody PhonePeInitiateRequest request) {

        final long start = System.nanoTime();

        // best-effort txnId for initiate: depends on your request fields (keep safe)
        // (no flow change; only MDC)
        try {
            // if your DTO has merchantTransactionId or transactionId, it will show here
            // if not, it will be null (no issue)
            String maybeTxn = null;
            try {
                // do not rely on getters existing; keep safe
                // (if getters exist, great; if not, just ignore)
                maybeTxn = (String) request.getClass().getMethod("getMerchantTransactionId").invoke(request);
            } catch (Exception ignore) { }
            if (maybeTxn == null) {
                try { maybeTxn = (String) request.getClass().getMethod("getTransactionId").invoke(request); }
                catch (Exception ignore) { }
            }
            putTxnId(maybeTxn);
        } catch (Exception ignore) { }

        try {
            MDC.put("service", SERVICE);
            MDC.put("controller", "PhonePePaymentController");
            MDC.put("endpoint", "/payments/phonepe/initiate");
            MDC.put("method", "POST");
            MDC.put("event", "request_received");

            log.info("Initiate payment request received");

            MDC.put("event", "business_start");
            log.info("Initiate payment processing started");

            PhonePeInitiateResponse resp = phonePePaymentService.initiatePayment(request);

            MDC.put("event", "response_sent");
            MDC.put("result", "SUCCESS");
            MDC.put("httpStatus", "200");
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
            log.info("Initiate payment response sent");

            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException ex) {

            MDC.put("event", "response_sent");
            MDC.put("result", "FAILED");
            MDC.put("httpStatus", "400");
            MDC.put("reason", ex.getMessage());
            MDC.put("error", ex.getClass().getSimpleName());
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
            log.warn("Initiate payment validation failed");

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));

        } catch (Exception ex) {

            MDC.put("event", "response_sent");
            MDC.put("result", "FAILED");
            MDC.put("httpStatus", "500");
            MDC.put("error", ex.getClass().getSimpleName());
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
            log.error("Initiate payment failed. err={}", ex.getMessage());

            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to initiate payment: " + ex.getMessage()
            ));
        }
    }

    // --------- CALLBACK (404 is happening here) ---------
    @PostMapping("/callback")
    public ResponseEntity<String> callback(@RequestBody Map<String, Object> body,
                                           @RequestHeader Map<String, String> headers) {

        final long start = System.nanoTime();

        // set base MDC fields for Grafana (no flow change)
        MDC.put("service", SERVICE);
        MDC.put("controller", "PhonePePaymentController");
        MDC.put("endpoint", "/payments/phonepe/callback");
        MDC.put("method", "POST");
        MDC.put("event", "request_received");

        // helpful: PhonePe code if present
        MDC.put("phonepeCode", safeStr(body == null ? null : body.get("code")));

        log.info("PhonePe callback received");

        try {
            MDC.put("event", "business_start");
            log.info("PhonePe callback processing started");

            Object dataObj = body.get("data");
            if (!(dataObj instanceof Map)) {

                MDC.put("event", "response_sent");
                MDC.put("result", "FAILED");
                MDC.put("httpStatus", "400");
                MDC.put("reason", "No data");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.warn("PhonePe callback invalid payload (no data)");

                return ResponseEntity.badRequest().body("No data");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;

            String merchantTransactionId = (String) data.get("merchantTransactionId");
            String code = (String) body.get("code");

            // This is the best txnId to correlate paymentservice logs + bookingservice logs
            putTxnId(merchantTransactionId);
            MDC.put("merchantTransactionId", merchantTransactionId == null ? null : shortTxn(merchantTransactionId));

            if (merchantTransactionId == null || merchantTransactionId.isBlank()) {

                MDC.put("event", "response_sent");
                MDC.put("result", "FAILED");
                MDC.put("httpStatus", "400");
                MDC.put("reason", "No merchantTransactionId");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.warn("PhonePe callback missing merchantTransactionId");

                return ResponseEntity.badRequest().body("No merchantTransactionId");
            }

            Optional<PendingPayment> opt =
                    pendingRepo.findByMerchantTransactionId(merchantTransactionId);

            MDC.put("pendingFound", String.valueOf(opt.isPresent()));

            if (opt.isEmpty()) {

                MDC.put("event", "response_sent");
                MDC.put("result", "SUCCESS");
                MDC.put("httpStatus", "200");
                MDC.put("reason", "Unknown transaction, ignoring");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.info("PhonePe callback ignored (unknown transaction)");

                return ResponseEntity.ok("Unknown transaction, ignoring");
            }

            PendingPayment pending = opt.get();

            boolean success = "PAYMENT_SUCCESS".equalsIgnoreCase(code)
                    || "SUCCESS".equalsIgnoreCase(code)
                    || "PAYMENT_COMPLETED".equalsIgnoreCase(code);

            MDC.put("paymentSuccess", String.valueOf(success));
            MDC.put("pendingStatusBefore", safeStr(pending.getStatus()));

            if (!success) {
                // Mark failed and return 200 so simulator/UI doesn’t break
                pending.setStatus("FAILED");
                pending.setUpdatedAt(LocalDateTime.now());
                pendingRepo.save(pending);

                MDC.put("pendingStatusAfter", "FAILED");
                MDC.put("event", "response_sent");
                MDC.put("result", "SUCCESS");
                MDC.put("httpStatus", "200");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.info("Payment marked as FAILED");

                return ResponseEntity.ok("Payment marked as FAILED");
            }

            // ---------- SUCCESS PATH ----------

            String bookingsJson = pending.getBookingsJson();
            MDC.put("hasBookingsJson", String.valueOf(bookingsJson != null && !bookingsJson.isBlank()));

            if (bookingsJson == null || bookingsJson.isBlank()) {
                pending.setStatus("FAILED");
                pending.setUpdatedAt(LocalDateTime.now());
                pendingRepo.save(pending);

                MDC.put("pendingStatusAfter", "FAILED");
                MDC.put("event", "response_sent");
                MDC.put("result", "SUCCESS");
                MDC.put("httpStatus", "200");
                MDC.put("reason", "No booking payload stored");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.warn("No booking payload stored in pending payment");

                return ResponseEntity.ok("No booking payload stored");
            }

            // DEV shortcut: if dummy test payload, skip hitting booking-service
            if (bookingsJson.contains("\"dummy\"")) {
                pending.setStatus("SUCCESS");
                pending.setUpdatedAt(LocalDateTime.now());
                pendingRepo.save(pending);

                MDC.put("pendingStatusAfter", "SUCCESS");
                MDC.put("event", "response_sent");
                MDC.put("result", "SUCCESS");
                MDC.put("httpStatus", "200");
                MDC.put("reason", "Dummy payload (skip booking-service)");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.info("Dummy payload – bookings not created");

                return ResponseEntity.ok("OK (dummy payload – bookings not created)");
            }

            // Real flow: forward to booking-service
            String url = "http://localhost:8082/bookings/bulk";

            HttpHeaders headersOut = new HttpHeaders();
            headersOut.setContentType(MediaType.APPLICATION_JSON);

            // ✅ propagate correlation + txn to booking-service (super important)
            // (does not change flow; only adds headers)
            String correlationId = MDC.get("correlationId");
            if (correlationId != null && !correlationId.isBlank()) {
                headersOut.set("X-Correlation-Id", correlationId);
            }
            if (merchantTransactionId != null && !merchantTransactionId.isBlank()) {
                headersOut.set("X-Txn-Id", merchantTransactionId.trim());
            }

            HttpEntity<String> entity = new HttpEntity<>(bookingsJson, headersOut);

            ResponseEntity<String> bookingResp;

            long t0 = System.nanoTime();
            try {
                MDC.put("event", "external_call_start");
                MDC.put("external", "bookingservice_bulk");
                MDC.put("externalUrl", url);
                log.info("Calling booking-service /bookings/bulk");

                bookingResp = restTemplate.postForEntity(url, entity, String.class);

                MDC.put("event", "external_call_end");
                MDC.put("externalElapsedMs", String.valueOf(elapsedMs(t0)));
                MDC.put("externalHttpStatus", String.valueOf(bookingResp.getStatusCode().value()));
                log.info("Booking-service call completed");

            } catch (org.springframework.web.client.HttpStatusCodeException ex) {
                // Booking-service returned 4xx/5xx
                MDC.put("event", "external_call_failed");
                MDC.put("external", "bookingservice_bulk");
                MDC.put("externalElapsedMs", String.valueOf(elapsedMs(t0)));
                MDC.put("externalHttpStatus", String.valueOf(ex.getStatusCode().value()));
                MDC.put("error", ex.getClass().getSimpleName());
                log.error("Booking-service returned error. status={}", ex.getStatusCode().value());

                ex.printStackTrace();
                pending.setStatus("FAILED");
                pending.setUpdatedAt(LocalDateTime.now());
                pendingRepo.save(pending);

                MDC.put("pendingStatusAfter", "FAILED");
                MDC.put("event", "response_sent");
                MDC.put("result", "SUCCESS"); // you intentionally return 200
                MDC.put("httpStatus", "200");
                MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
                log.info("Callback response sent (booking-service error handled)");

                // Still return 200 so simulator/UI shows a clean result
                return ResponseEntity.ok("Booking service error: " + ex.getStatusCode().value());
            }

            if (bookingResp.getStatusCode().is2xxSuccessful()) {
                pending.setStatus("SUCCESS");
            } else {
                pending.setStatus("FAILED");
            }

            pending.setUpdatedAt(LocalDateTime.now());
            pendingRepo.save(pending);

            MDC.put("pendingStatusAfter", pending.getStatus());
            MDC.put("event", "response_sent");
            MDC.put("result", "SUCCESS");
            MDC.put("httpStatus", "200");
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
            log.info("PhonePe callback response sent");

            return ResponseEntity.ok("OK");

        } catch (Exception ex) {
            MDC.put("event", "response_sent");
            MDC.put("result", "FAILED");
            MDC.put("httpStatus", "500");
            MDC.put("error", ex.getClass().getSimpleName());
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));
            log.error("PhonePe callback failed. err={}", ex.getMessage());

            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("ERROR");
        }
    }

}
//package com.carwash.paymentservice.controller;
//
//import com.carwash.paymentservice.dto.PhonePeInitiateRequest;
//import com.carwash.paymentservice.dto.PhonePeInitiateResponse;
//import com.carwash.paymentservice.entity.PendingPayment;
//import com.carwash.paymentservice.repository.PendingPaymentRepository;
//import com.carwash.paymentservice.service.PhonePePaymentService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Optional;
//
//@CrossOrigin(origins = "*")   // allow calls from 8080 simulator & 8082 UI
//@RestController
//@RequestMapping("/payments/phonepe")
//public class PhonePePaymentController {
//
//    private final PhonePePaymentService phonePePaymentService;
//    private final PendingPaymentRepository pendingRepo;
//    private final ObjectMapper objectMapper;
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public PhonePePaymentController(PhonePePaymentService phonePePaymentService,
//                                    PendingPaymentRepository pendingRepo,
//                                    ObjectMapper objectMapper) {
//        this.phonePePaymentService = phonePePaymentService;
//        this.pendingRepo = pendingRepo;
//        this.objectMapper = objectMapper;
//    }
//
//    // --------- INITIATE (already working) ---------
//    @PostMapping("/initiate")
//    public ResponseEntity<?> initiate(@RequestBody PhonePeInitiateRequest request) {
//        try {
//            PhonePeInitiateResponse resp = phonePePaymentService.initiatePayment(request);
//            return ResponseEntity.ok(resp);
//        } catch (IllegalArgumentException ex) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "success", false,
//                    "message", ex.getMessage()
//            ));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
//                    "success", false,
//                    "message", "Failed to initiate payment: " + ex.getMessage()
//            ));
//        }
//    }
//
//    // --------- CALLBACK (404 is happening here) ---------
//    @PostMapping("/callback")
//    public ResponseEntity<String> callback(@RequestBody Map<String, Object> body,
//                                           @RequestHeader Map<String, String> headers) {
//        try {
//            Object dataObj = body.get("data");
//            if (!(dataObj instanceof Map)) {
//                return ResponseEntity.badRequest().body("No data");
//            }
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> data = (Map<String, Object>) dataObj;
//
//            String merchantTransactionId = (String) data.get("merchantTransactionId");
//            String code = (String) body.get("code");
//
//            if (merchantTransactionId == null || merchantTransactionId.isBlank()) {
//                return ResponseEntity.badRequest().body("No merchantTransactionId");
//            }
//
//            Optional<PendingPayment> opt =
//                    pendingRepo.findByMerchantTransactionId(merchantTransactionId);
//
//            if (opt.isEmpty()) {
//                return ResponseEntity.ok("Unknown transaction, ignoring");
//            }
//
//            PendingPayment pending = opt.get();
//
//            boolean success = "PAYMENT_SUCCESS".equalsIgnoreCase(code)
//                    || "SUCCESS".equalsIgnoreCase(code)
//                    || "PAYMENT_COMPLETED".equalsIgnoreCase(code);
//
//            if (!success) {
//                // Mark failed and return 200 so simulator/UI doesn’t break
//                pending.setStatus("FAILED");
//                pending.setUpdatedAt(LocalDateTime.now());
//                pendingRepo.save(pending);
//                return ResponseEntity.ok("Payment marked as FAILED");
//            }
//
//            // ---------- SUCCESS PATH ----------
//
//            String bookingsJson = pending.getBookingsJson();
//            if (bookingsJson == null || bookingsJson.isBlank()) {
//                pending.setStatus("FAILED");
//                pending.setUpdatedAt(LocalDateTime.now());
//                pendingRepo.save(pending);
//                return ResponseEntity.ok("No booking payload stored");
//            }
//
//            // DEV shortcut: if dummy test payload, skip hitting booking-service
//            if (bookingsJson.contains("\"dummy\"")) {
//                pending.setStatus("SUCCESS");
//                pending.setUpdatedAt(LocalDateTime.now());
//                pendingRepo.save(pending);
//                return ResponseEntity.ok("OK (dummy payload – bookings not created)");
//            }
//
//            // Real flow: forward to booking-service
//            String url = "http://localhost:8082/bookings/bulk";
//
//            HttpHeaders headersOut = new HttpHeaders();
//            headersOut.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> entity = new HttpEntity<>(bookingsJson, headersOut);
//
//            ResponseEntity<String> bookingResp;
//            try {
//                bookingResp = restTemplate.postForEntity(url, entity, String.class);
//            } catch (org.springframework.web.client.HttpStatusCodeException ex) {
//                // Booking-service returned 4xx/5xx
//                ex.printStackTrace();
//                pending.setStatus("FAILED");
//                pending.setUpdatedAt(LocalDateTime.now());
//                pendingRepo.save(pending);
//                // Still return 200 so simulator/UI shows a clean result
//                return ResponseEntity.ok("Booking service error: " + ex.getStatusCode().value());
//            }
//
//            if (bookingResp.getStatusCode().is2xxSuccessful()) {
//                pending.setStatus("SUCCESS");
//            } else {
//                pending.setStatus("FAILED");
//            }
//
//            pending.setUpdatedAt(LocalDateTime.now());
//            pendingRepo.save(pending);
//
//            return ResponseEntity.ok("OK");
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return ResponseEntity.internalServerError().body("ERROR");
//        }
//    }
//
//}
