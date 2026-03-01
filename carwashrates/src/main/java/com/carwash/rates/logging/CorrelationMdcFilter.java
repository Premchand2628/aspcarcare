package com.carwash.rates.logging;

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

    // correlationId: from header or new
    String correlationId = request.getHeader(HDR_CORRELATION_ID);
    if (correlationId == null || correlationId.isBlank()) correlationId = newId();

    // reqId: always new per request (never equal to correlationId)
    String reqId = newId();

    // txnId: optional header propagation
    String txnId = request.getHeader(HDR_TXN_ID);

    try {
      MDC.put("correlationId", correlationId);
      MDC.put("reqId", reqId);

      // helpful common fields (optional but great for Grafana)
      MDC.put("method", request.getMethod());
      MDC.put("endpoint", request.getRequestURI());

      if (txnId != null && !txnId.isBlank()) MDC.put("txnId", txnId.trim());

      // also return correlationId back to client so UI / other services can reuse
      response.setHeader(HDR_CORRELATION_ID, correlationId);
      if (txnId != null && !txnId.isBlank()) response.setHeader(HDR_TXN_ID, txnId.trim());

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear(); // VERY IMPORTANT (prevents leakage between threads)
    }
  }
}
//package com.carwash.bookingservice.logging;
//
//import java.io.IOException;
//import java.util.UUID;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.slf4j.MDC;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class CorrelationMdcFilter extends OncePerRequestFilter {
//
//  // ✅ Standard headers to propagate across services
//  public static final String HDR_CORRELATION_ID = "X-Correlation-Id";
//  public static final String HDR_TXN_ID = "X-Txn-Id";
//  public static final String HDR_REQ_ID = "X-Req-Id";
//
//  // ✅ MDC keys (must be same everywhere)
//  public static final String MDC_CORRELATION_ID = "correlationId";
//  public static final String MDC_TXN_ID = "txnId";
//  public static final String MDC_REQ_ID = "reqId";
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request,
//                                  HttpServletResponse response,
//                                  FilterChain filterChain) throws ServletException, IOException {
//
//    String correlationId = firstNonBlank(
//        request.getHeader(HDR_CORRELATION_ID),
//        request.getHeader(HDR_REQ_ID) // allow old dashboards to use req id
//    );
//
//    if (correlationId == null) {
//      correlationId = UUID.randomUUID().toString().replace("-", "");
//    }
//
//    String reqId = firstNonBlank(request.getHeader(HDR_REQ_ID), correlationId);
//    String txnId = trimToNull(request.getHeader(HDR_TXN_ID));
//
//    try {
//      MDC.put(MDC_CORRELATION_ID, correlationId);
//      MDC.put(MDC_REQ_ID, reqId);
//      if (txnId != null) MDC.put(MDC_TXN_ID, txnId);
//
//      // Return headers back so UI / gateway can read them
//      response.setHeader(HDR_CORRELATION_ID, correlationId);
//      response.setHeader(HDR_REQ_ID, reqId);
//      if (txnId != null) response.setHeader(HDR_TXN_ID, txnId);
//
//      filterChain.doFilter(request, response);
//
//    } finally {
//      MDC.remove(MDC_CORRELATION_ID);
//      MDC.remove(MDC_REQ_ID);
//      MDC.remove(MDC_TXN_ID);
//    }
//  }
//
//  private static String trimToNull(String s) {
//    if (s == null) return null;
//    String t = s.trim();
//    return t.isEmpty() ? null : t;
//  }
//
//  private static String firstNonBlank(String a, String b) {
//    String ta = trimToNull(a);
//    if (ta != null) return ta;
//    return trimToNull(b);
//  }
//}
