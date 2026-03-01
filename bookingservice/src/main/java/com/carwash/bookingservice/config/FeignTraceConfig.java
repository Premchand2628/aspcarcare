package com.carwash.bookingservice.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignTraceConfig {

    @Bean
    public RequestInterceptor traceHeaders() {
        return template -> {
            // Read from MDC (set by RequestTracingFilter)
            String corrId = MDC.get(RequestTracingFilter.MDC_CORR);
            String reqId  = MDC.get(RequestTracingFilter.MDC_REQ);

            if (corrId != null && !corrId.isBlank()) {
                template.header(RequestTracingFilter.HDR_CORR, corrId);
            }
            // For downstream services, you can keep same reqId OR create new one.
            // Keeping same reqId makes it easy to trace a single user action across services.
            if (reqId != null && !reqId.isBlank()) {
                template.header(RequestTracingFilter.HDR_REQ, reqId);
            }
        };
    }
}
