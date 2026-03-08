package com.carwash.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ReactiveHttpLoggingFilter implements WebFilter {

  private static final Logger log = LoggerFactory.getLogger(ReactiveHttpLoggingFilter.class);
  private static final int MAX_BODY_CHARS = 8000;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (path.startsWith("/actuator/health") || path.startsWith("/actuator/info")) {
      return chain.filter(exchange);
    }

    long start = System.currentTimeMillis();
    String requestId = UUID.randomUUID().toString();

    AtomicReference<String> requestBodyRef = new AtomicReference<>("");
    AtomicReference<String> responseBodyRef = new AtomicReference<>("");

    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

    ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
      @Override
      public Flux<DataBuffer> getBody() {
        MediaType mediaType = getHeaders().getContentType();
        if (!isLoggableContentType(mediaType)) {
          return super.getBody();
        }

        return super.getBody().map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          appendLimited(requestBodyRef, new String(bytes, StandardCharsets.UTF_8));
          DataBufferUtils.release(dataBuffer);
          return bufferFactory.wrap(bytes);
        });
      }
    };

    ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
      @Override
      public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
        MediaType mediaType = getHeaders().getContentType();
        if (!isLoggableContentType(mediaType)) {
          return super.writeWith(body);
        }

        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
        return super.writeWith(fluxBody.map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          appendLimited(responseBodyRef, new String(bytes, StandardCharsets.UTF_8));
          DataBufferUtils.release(dataBuffer);
          return bufferFactory.wrap(bytes);
        }));
      }
    };

    ServerWebExchange mutatedExchange = exchange.mutate()
        .request(requestDecorator)
        .response(responseDecorator)
        .build();

    return chain.filter(mutatedExchange)
        .doFinally(signalType -> {
          long durationMs = System.currentTimeMillis() - start;

          String method = exchange.getRequest().getMethod() != null
              ? exchange.getRequest().getMethod().name()
              : "UNKNOWN";

          String query = exchange.getRequest().getURI().getQuery();
          int status = responseDecorator.getStatusCode() != null
              ? responseDecorator.getStatusCode().value()
              : 200;

          Map<String, String> requestHeaders = collectRequestHeaders(exchange.getRequest().getHeaders());
          Map<String, String> responseHeaders = collectResponseHeaders(responseDecorator.getHeaders());

          log.info(
              "HTTP_REQUEST requestId={} method={} path={} query={} headers={} body={}",
              requestId, method, path, safe(query), requestHeaders, safe(requestBodyRef.get())
          );

          log.info(
              "HTTP_RESPONSE requestId={} method={} path={} status={} durationMs={} headers={} body={}",
              requestId, method, path, status, durationMs, responseHeaders, safe(responseBodyRef.get())
          );
        });
  }

  private void appendLimited(AtomicReference<String> target, String chunk) {
    if (!StringUtils.hasText(chunk)) {
      return;
    }
    String current = target.get();
    String merged = current + chunk;
    if (merged.length() > MAX_BODY_CHARS) {
      merged = merged.substring(0, MAX_BODY_CHARS) + "...(truncated)";
    }
    target.set(merged);
  }

  private Map<String, String> collectRequestHeaders(HttpHeaders headers) {
    Map<String, String> map = new LinkedHashMap<>();
    putIfPresent(map, "content-type", headers.getFirst(HttpHeaders.CONTENT_TYPE));
    putIfPresent(map, "user-agent", headers.getFirst(HttpHeaders.USER_AGENT));
    putIfPresent(map, "x-forwarded-for", headers.getFirst("X-Forwarded-For"));
    putIfPresent(map, "authorization", maskAuthorization(headers.getFirst(HttpHeaders.AUTHORIZATION)));
    return map;
  }

  private Map<String, String> collectResponseHeaders(HttpHeaders headers) {
    Map<String, String> map = new LinkedHashMap<>();
    putIfPresent(map, "content-type", headers.getFirst(HttpHeaders.CONTENT_TYPE));
    putIfPresent(map, "cache-control", headers.getFirst(HttpHeaders.CACHE_CONTROL));
    return map;
  }

  private void putIfPresent(Map<String, String> target, String key, String value) {
    if (StringUtils.hasText(value)) {
      target.put(key, value);
    }
  }

  private String maskAuthorization(String authHeader) {
    if (!StringUtils.hasText(authHeader)) return authHeader;
    if (authHeader.toLowerCase().startsWith("bearer ")) {
      return "Bearer ***";
    }
    return "***";
  }

  private boolean isLoggableContentType(MediaType contentType) {
    if (contentType == null) return true;
    String ct = contentType.toString().toLowerCase();
    return ct.contains("application/json")
        || ct.contains("application/xml")
        || ct.contains("application/x-www-form-urlencoded")
        || ct.contains("text/")
        || ct.contains("application/problem+json");
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
