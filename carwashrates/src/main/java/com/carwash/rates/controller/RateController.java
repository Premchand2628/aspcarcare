package com.carwash.rates.controller;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.dto.RateResponse;
import com.carwash.rates.dto.RateUpsertRequest;
import com.carwash.rates.entity.Rate;
import com.carwash.rates.repository.RateRepository;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rates")
@CrossOrigin(origins = "*")
public class RateController {

    private static final Logger log = LoggerFactory.getLogger(RateController.class);
    private static final String SERVICE = "rateservice";

    private final RateRepository rateRepository;

    public RateController(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static String safe(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    // ==========================================================
    // GET ALL
    // ==========================================================
    @GetMapping("/all")
    public ResponseEntity<?> getAllRates() {

        final long start = System.nanoTime();

        MDC.put("service", SERVICE);
        MDC.put("controller", "RateController");
        MDC.put("endpoint", "/rates/all");
        MDC.put("method", "GET");
        MDC.put("event", "request_received");

        log.info("Get all rates request received");

        MDC.put("event", "business_start");
        List<Rate> list = rateRepository.findAll();

        MDC.put("count", String.valueOf(list == null ? 0 : list.size()));
        MDC.put("event", "response_sent");
        MDC.put("result", "SUCCESS");
        MDC.put("httpStatus", "200");
        MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

        log.info("Get all rates response sent");

        return ResponseEntity.ok(list);
    }

    // ==========================================================
    // GET SINGLE RATE
    // ==========================================================
    @GetMapping
    public ResponseEntity<RateResponse> getRate(
            @RequestParam String vehicleType,
            @RequestParam String washLevel
    ) {

        final long start = System.nanoTime();

        MDC.put("service", SERVICE);
        MDC.put("controller", "RateController");
        MDC.put("endpoint", "/rates");
        MDC.put("method", "GET");
        MDC.put("event", "request_received");
        MDC.put("vehicleType", vehicleType);
        MDC.put("washLevel", washLevel);

        log.info("Get rate request received");

        try {
            MDC.put("event", "business_start");

            VehicleType vt = VehicleType.from(vehicleType);
            WashLevel wl = WashLevel.from(washLevel);

            MDC.put("vehicleTypeNorm", vt.name());
            MDC.put("washLevelNorm", wl.name());

            return rateRepository
                    .findTopByVehicleTypeAndWashLevelAndActiveTrue(vt, wl)
                    .map(r -> {

                        MDC.put("event", "response_sent");
                        MDC.put("result", "SUCCESS");
                        MDC.put("httpStatus", "200");
                        MDC.put("amount", safe(r.getAmount()));
                        MDC.put("currency", safe(r.getCurrency()));
                        MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

                        log.info("Rate found");

                        return ResponseEntity.ok(
                                new RateResponse(
                                        vt.name(),
                                        wl.name(),
                                        r.getAmount(),
                                        r.getCurrency()
                                )
                        );
                    })
                    .orElseGet(() -> {

                        MDC.put("event", "response_sent");
                        MDC.put("result", "NOT_FOUND");
                        MDC.put("httpStatus", "404");
                        MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

                        log.warn("Rate not found");

                        return ResponseEntity.status(404).build();
                    });

        } catch (Exception ex) {

            MDC.put("event", "response_sent");
            MDC.put("result", "FAILED");
            MDC.put("httpStatus", "400");
            MDC.put("error", ex.getClass().getSimpleName());
            MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

            log.warn("Get rate failed. err={}", ex.getMessage());
            throw ex;
        }
    }

    // ==========================================================
    // GET MATRIX
    // ==========================================================
    @GetMapping("/matrix")
    public List<RateResponse> getMatrix() {

        final long start = System.nanoTime();

        MDC.put("service", SERVICE);
        MDC.put("controller", "RateController");
        MDC.put("endpoint", "/rates/matrix");
        MDC.put("method", "GET");
        MDC.put("event", "request_received");

        log.info("Rate matrix request received");

        MDC.put("event", "business_start");

        List<RateResponse> list =
                rateRepository.findByActiveTrueOrderByVehicleTypeAscWashLevelAsc()
                        .stream()
                        .map(r -> new RateResponse(
                                r.getVehicleType().name(),
                                r.getWashLevel().name(),
                                r.getAmount(),
                                r.getCurrency()
                        ))
                        .collect(Collectors.toList());

        MDC.put("count", String.valueOf(list.size()));
        MDC.put("event", "response_sent");
        MDC.put("result", "SUCCESS");
        MDC.put("httpStatus", "200");
        MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

        log.info("Rate matrix response sent");

        return list;
    }

    // ==========================================================
    // UPSERT
    // ==========================================================
    @PostMapping
    public ResponseEntity<?> upsert(@Valid @RequestBody RateUpsertRequest req) {

        final long start = System.nanoTime();

        MDC.put("service", SERVICE);
        MDC.put("controller", "RateController");
        MDC.put("endpoint", "/rates");
        MDC.put("method", "POST");
        MDC.put("event", "request_received");
        MDC.put("vehicleType", safe(req.getVehicleType()));
        MDC.put("washLevel", safe(req.getWashLevel()));
        MDC.put("amount", safe(req.getAmount()));

        log.info("Rate upsert request received");

        MDC.put("event", "business_start");

        VehicleType vt = VehicleType.from(req.getVehicleType());
        WashLevel wl = WashLevel.from(req.getWashLevel());

        Rate rate = rateRepository
                .findTopByVehicleTypeAndWashLevelAndActiveTrue(vt, wl)
                .orElse(new Rate());

        boolean isUpdate = rate.getId() != null;

        rate.setVehicleType(vt);
        rate.setWashLevel(wl);
        rate.setAmount(req.getAmount());
        rate.setActive(true);

        rateRepository.save(rate);

        MDC.put("event", "response_sent");
        MDC.put("result", "SUCCESS");
        MDC.put("httpStatus", "200");
        MDC.put("operation", isUpdate ? "UPDATE" : "CREATE");
        MDC.put("elapsedMs", String.valueOf(elapsedMs(start)));

        log.info("Rate saved");

        return ResponseEntity.ok("Rate saved");
    }
}
//package com.carwash.rates.controller;
//
//import com.carwash.rates.common.VehicleType;
//import com.carwash.rates.common.WashLevel;
//import com.carwash.rates.dto.RateResponse;
//import com.carwash.rates.dto.RateUpsertRequest;
//import com.carwash.rates.entity.Rate;
//import com.carwash.rates.repository.RateRepository;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/rates")
//@CrossOrigin(origins = "*")
//public class RateController {
//
//    private final RateRepository rateRepository;
//
//    public RateController(RateRepository rateRepository) {
//        this.rateRepository = rateRepository;
//    }
//    @GetMapping("/all")
//    public ResponseEntity<?> getAllRates() {
//      return ResponseEntity.ok(rateRepository.findAll());
//    }
//
//    // GET single rate
//    @GetMapping
//    public ResponseEntity<RateResponse> getRate(
//            @RequestParam String vehicleType,
//            @RequestParam String washLevel
//    ) {
//        VehicleType vt = VehicleType.from(vehicleType);
//        WashLevel wl = WashLevel.from(washLevel);
//
//        return rateRepository
//                .findTopByVehicleTypeAndWashLevelAndActiveTrue(vt, wl)
//                .map(r -> ResponseEntity.ok(
//                        new RateResponse(
//                                vt.name(),
//                                wl.name(),
//                                r.getAmount(),
//                                r.getCurrency()
//                        )
//                ))
//                .orElseGet(() ->
//                        ResponseEntity.status(404).build()
//                );
//    }
//
//
//    // GET matrix
//    @GetMapping("/matrix")
//    public List<RateResponse> getMatrix() {
//        return rateRepository.findByActiveTrueOrderByVehicleTypeAscWashLevelAsc()
//                .stream()
//                .map(r -> new RateResponse(
//                        r.getVehicleType().name(),
//                        r.getWashLevel().name(),
//                        r.getAmount(),
//                        r.getCurrency()
//                ))
//                .collect(Collectors.toList());
//    }
//
//    // UPSERT (admin)
//    @PostMapping
//    public ResponseEntity<?> upsert(@Valid @RequestBody RateUpsertRequest req) {
//
//        VehicleType vt = VehicleType.from(req.getVehicleType());
//        WashLevel wl = WashLevel.from(req.getWashLevel());
//
//        Rate rate = rateRepository
//                .findTopByVehicleTypeAndWashLevelAndActiveTrue(vt, wl)
//                .orElse(new Rate());
//
//        rate.setVehicleType(vt);
//        rate.setWashLevel(wl);
//        rate.setAmount(req.getAmount());
//        rate.setActive(true);
//
//        rateRepository.save(rate);
//        return ResponseEntity.ok("Rate saved");
//    }
//}
