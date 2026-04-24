package com.carwashcommon.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class HttpRequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(HttpRequestResponseLoggingFilter.class);
  private static final int MAX_BODY_CHARS = 2048;
  private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

  // Paths whose request/response bodies must NEVER be logged (OTP, passwords, payment payloads, admin ops).
  private static final List<String> BODY_LOG_DENY_PREFIXES = List.of(
      "/auth/",
      "/payments/",
      "/admin/",
      "/staff/",
      "/users/me/password"
  );

  // JSON field-name pattern whose values will be masked everywhere else.
  // Matches   "otp":"1234"   "otp" : "1234"   "otp":1234  (numbers or strings).
  private static final Pattern SENSITIVE_JSON_FIELD = Pattern.compile(
      "(?i)(\"(?:otp|password|newPassword|confirmPassword|currentPassword|token|refreshToken|accessToken|" +
          "mobileNumber|phone|email|cardNumber|cvv|saltKey|clientSecret|jwt|secret|authorization)\"\\s*:\\s*)" +
          "(\"[^\"]*\"|-?\\d+(?:\\.\\d+)?)"
  );
  private static final String MASKED_VALUE = "\"***\"";

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
    String transactionId = resolveTransactionId(requestWrapper);
    responseWrapper.setHeader(TRANSACTION_ID_HEADER, transactionId);

    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } finally {
      long durationMs = System.currentTimeMillis() - start;

      String method = requestWrapper.getMethod();
      String path = requestWrapper.getRequestURI();
      String query = requestWrapper.getQueryString();
      int status = responseWrapper.getStatus();
      boolean sensitivePath = isSensitivePath(path);

      Map<String, String> requestHeaders = collectRequestHeaders(requestWrapper);
      Map<String, String> responseHeaders = collectResponseHeaders(responseWrapper);

      String requestBody = bodyForLog(
          requestWrapper.getContentAsByteArray(),
          requestWrapper.getCharacterEncoding(),
          requestWrapper.getContentType(),
          sensitivePath
      );

      String responseBody = bodyForLog(
          responseWrapper.getContentAsByteArray(),
          responseWrapper.getCharacterEncoding(),
          responseWrapper.getContentType(),
          sensitivePath
      );

      log.info(
          "HTTP_REQUEST transactionId={} method={} path={} query={} headers={} body={}",
          transactionId, method, path, safe(query), requestHeaders, requestBody
      );

      log.info(
          "HTTP_RESPONSE transactionId={} method={} path={} status={} durationMs={} headers={} body={}",
          transactionId, method, path, status, durationMs, responseHeaders, responseBody
      );

      responseWrapper.copyBodyToResponse();
    }
  }

  private Map<String, String> collectRequestHeaders(HttpServletRequest request) {
    Map<String, String> headers = new LinkedHashMap<>();
    putIfPresent(headers, "content-type", request.getHeader("Content-Type"));
    putIfPresent(headers, "user-agent", request.getHeader("User-Agent"));
    putIfPresent(headers, "x-forwarded-for", request.getHeader("X-Forwarded-For"));
    putIfPresent(headers, "x-transaction-id", request.getHeader(TRANSACTION_ID_HEADER));
    putIfPresent(headers, "authorization", maskAuthorization(request.getHeader("Authorization")));
    return headers;
  }

  private Map<String, String> collectResponseHeaders(HttpServletResponse response) {
    Map<String, String> headers = new LinkedHashMap<>();
    putIfPresent(headers, "content-type", response.getHeader("Content-Type"));
    putIfPresent(headers, "cache-control", response.getHeader("Cache-Control"));
    putIfPresent(headers, "x-transaction-id", response.getHeader(TRANSACTION_ID_HEADER));
    return headers;
  }

  private String resolveTransactionId(HttpServletRequest request) {
    String incoming = request.getHeader(TRANSACTION_ID_HEADER);
    if (StringUtils.hasText(incoming)) {
      return incoming.trim();
    }
    return UUID.randomUUID().toString();
  }

  private void putIfPresent(Map<String, String> target, String key, String value) {
    if (StringUtils.hasText(value)) {
      target.put(key, value);
    }
  }

  private boolean isSensitivePath(String path) {
    if (path == null) return false;
    for (String prefix : BODY_LOG_DENY_PREFIXES) {
      if (path.startsWith(prefix)) return true;
    }
    return false;
  }

  /**
   * Returns a body suitable for logging:
   *  - empty string for empty bodies
   *  - "&lt;redacted&gt;" for any path in the sensitive deny-list
   *  - "&lt;non-text-payload&gt;" for binary
   *  - masked + truncated JSON/text otherwise
   */
  private String bodyForLog(byte[] content, String encoding, String contentType, boolean sensitivePath) {
    if (content == null || content.length == 0) {
      return "";
    }
    if (sensitivePath) {
      return "<redacted>";
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
    body = maskSensitiveJsonFields(body);
    if (body.length() > MAX_BODY_CHARS) {
      return body.substring(0, MAX_BODY_CHARS) + "...(truncated)";
    }
    return body;
  }

  private String maskSensitiveJsonFields(String body) {
    if (body == null || body.isEmpty()) return body;
    return SENSITIVE_JSON_FIELD.matcher(body).replaceAll("$1" + MASKED_VALUE);
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
