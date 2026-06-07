package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/stats")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final BookingRepository bookingRepository;

    /**
     * GET /admin/stats/overview
     * Returns all dashboard stats in one call.
     */
    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        List<String> revenueStatuses = List.of("CONFIRMED", "IN_SERVICING", "COMPLETED", "CLOSED");

        Map<String, Object> stats = new LinkedHashMap<>();

        // ---- Total counts ----
        stats.put("totalBookings", bookingRepository.count());
        stats.put("todayBookings", bookingRepository.countByBookingDate(today));
        stats.put("last7DaysBookings", bookingRepository.countCreatedSince(todayStart.minusDays(7)));
        stats.put("last30DaysBookings", bookingRepository.countCreatedSince(todayStart.minusDays(30)));

        // ---- Status breakdown ----
        Map<String, Long> statusBreakdown = new LinkedHashMap<>();
        for (String s : List.of("PENDING", "CONFIRMED", "IN_SERVICING", "COMPLETED", "CANCELLED", "CLOSED")) {
            statusBreakdown.put(s, bookingRepository.countByStatus(s));
        }
        stats.put("statusBreakdown", statusBreakdown);

        // ---- Revenue ----
        stats.put("totalRevenue", bookingRepository.sumRevenueByStatuses(revenueStatuses));
        stats.put("todayRevenue", bookingRepository.sumRevenueByStatusesAndDate(revenueStatuses, today));

        // ---- Users ----
        stats.put("uniqueCustomers", bookingRepository.countDistinctUsers());
        stats.put("todayCustomers", bookingRepository.countDistinctUsersByDate(today));

        // ---- Daily trend (last 30 days) ----
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        for (Object[] row : bookingRepository.countPerDay(thirtyDaysAgo)) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", row[0].toString());
            day.put("count", ((Number) row[1]).longValue());
            dailyTrend.add(day);
        }
        stats.put("dailyTrend", dailyTrend);

        // ---- Breakdowns ----
        stats.put("byWashType", toBreakdown(bookingRepository.countByWashType()));
        stats.put("byCarType", toBreakdown(bookingRepository.countByCarType()));
        stats.put("byServiceType", toBreakdown(bookingRepository.countByServiceType()));
        stats.put("byCentre", toBreakdown(bookingRepository.countByCentre()));

        return stats;
    }

    private List<Map<String, Object>> toBreakdown(List<Object[]> rows) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : rows) {
            list.add(Map.of(
                    "label", row[0] != null ? row[0].toString() : "UNKNOWN",
                    "count", ((Number) row[1]).longValue()
            ));
        }
        return list;
    }
}
