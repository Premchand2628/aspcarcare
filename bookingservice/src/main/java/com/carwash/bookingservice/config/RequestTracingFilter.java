package com.carwash.bookingservice.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTracingFilter.class);

    // ✅ Keep consistent key names everywhere
    public static final String HDR_CORR = "x-correlation-id";
    public static final String HDR_REQ  = "x-request-id";

    // MDC keys used by your JSON logs
    public static final String MDC_CORR = "correlationId";
    public static final String MDC_REQ  = "reqId";

    private static final String SERVICE = "bookingservice";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startNanos = System.nanoTime();

        // 1) correlation-id: accept from client if present, else create
        String corrId = trimToNull(request.getHeader(HDR_CORR));
        if (corrId == null) corrId = uuidNoDash();

        // 2) request-id: always per-request/hop (new each time)
        String reqId = trimToNull(request.getHeader(HDR_REQ));
        if (reqId == null) reqId = uuidNoDash();

        // Put into MDC so every log line includes it automatically
        MDC.put("service", SERVICE);
        MDC.put(MDC_CORR, corrId);
        MDC.put(MDC_REQ, reqId);

        // Helpful request context
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String endpoint = (query == null || query.isBlank()) ? path : (path + "?" + query);

        MDC.put("method", method);
        MDC.put("endpoint", path);

        // Also return ids to browser so you can see it in Network tab
        response.setHeader(HDR_CORR, corrId);
        response.setHeader(HDR_REQ, reqId);

        // Log once (avoid duplicates)
        MDC.put("event", "request_received");
        log.info("HTTP request received");

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

            MDC.put("event", "response_sent");
            MDC.put("httpStatus", String.valueOf(response.getStatus()));
            MDC.put("elapsedMs", String.valueOf(elapsedMs));
            log.info("HTTP response sent");

            MDC.clear();
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String uuidNoDash() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
