package com.carwash.mailnotification.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class HttpRequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(HttpRequestResponseLoggingFilter.class);
  private static final int MAX_BODY_CHARS = 8000;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/actuator/health") || uri.startsWith("/actuator/info");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    ContentCachingRequestWrapper requestWrapper = request instanceof ContentCachingRequestWrapper
        ? (ContentCachingRequestWrapper) request
        : new ContentCachingRequestWrapper(request);

    ContentCachingResponseWrapper responseWrapper = response instanceof ContentCachingResponseWrapper
        ? (ContentCachingResponseWrapper) response
        : new ContentCachingResponseWrapper(response);

    long start = System.currentTimeMillis();
    String requestId = UUID.randomUUID().toString();

    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      long durationMs = System.currentTimeMillis() - start;

      String method = requestWrapper.getMethod();
      String path = requestWrapper.getRequestURI();
      String query = requestWrapper.getQueryString();
      int status = responseWrapper.getStatus();

      Map<String, String> requestHeaders = collectRequestHeaders(requestWrapper);
      Map<String, String> responseHeaders = collectResponseHeaders(responseWrapper);

      String requestBody = extractBody(
          requestWrapper.getContentAsByteArray(),
          requestWrapper.getCharacterEncoding(),
          requestWrapper.getContentType()
      );

      String responseBody = extractBody(
          responseWrapper.getContentAsByteArray(),
          responseWrapper.getCharacterEncoding(),
          responseWrapper.getContentType()
      );

      log.info(
          "HTTP_REQUEST requestId={} method={} path={} query={} headers={} body={}",
          requestId, method, path, safe(query), requestHeaders, requestBody
      );

      log.info(
          "HTTP_RESPONSE requestId={} method={} path={} status={} durationMs={} headers={} body={}",
          requestId, method, path, status, durationMs, responseHeaders, responseBody
      );

      responseWrapper.copyBodyToResponse();
    }
  }

  private Map<String, String> collectRequestHeaders(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<>();
    putIfPresent(headers, "content-type", request.getHeader("Content-Type"));
    putIfPresent(headers, "user-agent", request.getHeader("User-Agent"));
    putIfPresent(headers, "x-forwarded-for", request.getHeader("X-Forwarded-For"));
    putIfPresent(headers, "authorization", maskAuthorization(request.getHeader("Authorization")));
    return headers;
  }

  private Map<String, String> collectResponseHeaders(HttpServletResponse response) {
    Map<String, String> headers = new LinkedHashMap<>();
    putIfPresent(headers, "content-type", response.getHeader("Content-Type"));
    putIfPresent(headers, "cache-control", response.getHeader("Cache-Control"));
    return headers;
  }

  private void putIfPresent(Map<String, String> target, String key, String value) {
    if (StringUtils.hasText(value)) {
      target.put(key, value);
    }
  }

  private String extractBody(byte[] content, String encoding, String contentType) {
    if (content == null || content.length == 0) {
      return "";
    }

    if (!isLoggableContentType(contentType)) {
      return "<non-text-payload>";
    }

    Charset charset;
    try {
      charset = StringUtils.hasText(encoding) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
    } catch (Exception ex) {
      charset = StandardCharsets.UTF_8;
    }

    String body = new String(content, charset);
    if (body.length() > MAX_BODY_CHARS) {
      return body.substring(0, MAX_BODY_CHARS) + "...(truncated)";
    }
    return body;
  }

  private boolean isLoggableContentType(String contentType) {
    if (!StringUtils.hasText(contentType)) return true;
    String ct = contentType.toLowerCase();
    return ct.contains("application/json")
        || ct.contains("application/xml")
        || ct.contains("application/x-www-form-urlencoded")
        || ct.contains("text/")
        || ct.contains("application/problem+json");
  }

  private String maskAuthorization(String authHeader) {
    if (!StringUtils.hasText(authHeader)) return authHeader;
    if (authHeader.toLowerCase().startsWith("bearer ")) {
      return "Bearer ***";
    }
    return "***";
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
