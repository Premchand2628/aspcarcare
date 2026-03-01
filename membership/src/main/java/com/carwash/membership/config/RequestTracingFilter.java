package com.carwash.membership.config;

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

  public static final String HDR_CORR = "x-correlation-id";
  public static final String HDR_REQ  = "x-request-id";

  public static final String MDC_CORR = "correlationId";
  public static final String MDC_REQ  = "reqId";

  private static final String SERVICE = "membershipservice";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    long startNanos = System.nanoTime();

    String corrId = trimToNull(request.getHeader(HDR_CORR));
    if (corrId == null) corrId = uuidNoDash();

    String reqId = trimToNull(request.getHeader(HDR_REQ));
    if (reqId == null) reqId = uuidNoDash();

    MDC.put("service", SERVICE);
    MDC.put(MDC_CORR, corrId);
    MDC.put(MDC_REQ, reqId);

    MDC.put("method", request.getMethod());
    MDC.put("endpoint", request.getRequestURI());

    response.setHeader(HDR_CORR, corrId);
    response.setHeader(HDR_REQ, reqId);

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