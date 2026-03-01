package com.carwash.rates.logging;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class FeignMdcRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {

    // ✅ MDC keys (strings)
    String correlationId = MDC.get("correlationId");
    String txnId = MDC.get("txnId");

    // ✅ Headers must match CorrelationMdcFilter
    if (correlationId != null && !correlationId.isBlank()) {
      template.header(CorrelationMdcFilter.HDR_CORRELATION_ID, correlationId);
    }
    if (txnId != null && !txnId.isBlank()) {
      template.header(CorrelationMdcFilter.HDR_TXN_ID, txnId);
    }

    // ❌ DO NOT forward reqId (each service creates its own reqId)
  }
}
