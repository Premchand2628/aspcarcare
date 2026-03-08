package com.carwash.bookingservice.logging;

import org.slf4j.MDC;

public final class RequestTimeline {
  private RequestTimeline(){}

  public static void markStart() {
    MDC.put("reqStartNs", String.valueOf(System.nanoTime()));
  }

  public static long elapsedMs() {
    String v = MDC.get("reqStartNs");
    if (v == null) return -1;
    long startNs = Long.parseLong(v);
    return (System.nanoTime() - startNs) / 1_000_000;
  }
}
