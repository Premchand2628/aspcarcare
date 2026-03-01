package com.carwash.paymentservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationMdcFilter extends OncePerRequestFilter {

  public static final String HDR_CORRELATION_ID = "X-Correlation-Id";
  public static final String HDR_TXN_ID = "X-Txn-Id";

  private static String newId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String correlationId = request.getHeader(HDR_CORRELATION_ID);
    if (correlationId == null || correlationId.isBlank()) correlationId = newId();

    // Always new per service request
    String reqId = newId();

    String txnId = request.getHeader(HDR_TXN_ID);

    try {
      MDC.put("correlationId", correlationId);
      MDC.put("reqId", reqId);

      MDC.put("method", request.getMethod());
      MDC.put("endpoint", request.getRequestURI());

      if (txnId != null && !txnId.isBlank()) MDC.put("txnId", txnId.trim());

      response.setHeader(HDR_CORRELATION_ID, correlationId);
      if (txnId != null && !txnId.isBlank()) response.setHeader(HDR_TXN_ID, txnId.trim());

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
