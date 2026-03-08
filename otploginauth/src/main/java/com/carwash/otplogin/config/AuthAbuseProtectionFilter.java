package com.carwash.otplogin.config;

import com.carwash.otplogin.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthAbuseProtectionFilter extends OncePerRequestFilter {

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.security.request.max-body-bytes:16384}")
    private int maxBodyBytes;

    @Value("${app.security.rate-limit.send-otp.max-requests:5}")
    private int sendOtpMaxRequests;

    @Value("${app.security.rate-limit.send-otp.window-seconds:600}")
    private int sendOtpWindowSeconds;

    @Value("${app.security.rate-limit.verify-otp.max-requests:12}")
    private int verifyOtpMaxRequests;

    @Value("${app.security.rate-limit.verify-otp.window-seconds:600}")
    private int verifyOtpWindowSeconds;

    @Value("${app.security.rate-limit.google-login.max-requests:30}")
    private int googleLoginMaxRequests;

    @Value("${app.security.rate-limit.google-login.window-seconds:300}")
    private int googleLoginWindowSeconds;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest requestToUse = request;
        if (isBodyMethod(request.getMethod())) {
            long contentLength = request.getContentLengthLong();
            if (contentLength > maxBodyBytes) {
                writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Request body too large");
                return;
            }

            try {
                requestToUse = new CachedBodyRequestWrapper(request, maxBodyBytes);
            } catch (PayloadTooLargeException ex) {
                writeError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Request body too large");
                return;
            }
        }

        RatePolicy policy = ratePolicy(requestToUse);
        if (policy != null) {
            String key = policy.keyPrefix + "|" + clientIp(requestToUse);
            if (!allowRequest(key, policy.maxRequests, policy.windowSeconds)) {
                response.setHeader("Retry-After", String.valueOf(policy.windowSeconds));
                writeError(response, 429, "Too many requests. Please try again later.");
                return;
            }
        }

        filterChain.doFilter(requestToUse, response);
    }

    private boolean isBodyMethod(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private RatePolicy ratePolicy(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }

        if ("/auth/login/send-otp".equals(uri)
                || "/auth/signup/send-otp".equals(uri)
                || "/auth/password/forgot/send-otp".equals(uri)) {
            return new RatePolicy("send-otp", sendOtpMaxRequests, sendOtpWindowSeconds);
        }

        if ("/auth/verify-otp".equals(uri)) {
            return new RatePolicy("verify-otp", verifyOtpMaxRequests, verifyOtpWindowSeconds);
        }

        if ("/auth/login/google".equals(uri)) {
            return new RatePolicy("google-login", googleLoginMaxRequests, googleLoginWindowSeconds);
        }

        return null;
    }

    private boolean allowRequest(String key, int maxRequests, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter(now));

        synchronized (counter) {
            if (now - counter.windowStartEpochSeconds >= windowSeconds) {
                counter.windowStartEpochSeconds = now;
                counter.count = 0;
            }

            if (counter.count >= maxRequests) {
                return false;
            }

            counter.count++;
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIdx = forwarded.indexOf(',');
            return commaIdx > 0 ? forwarded.substring(0, commaIdx).trim() : forwarded.trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), new ApiResponse(false, message));
    }

    private static class WindowCounter {
        private long windowStartEpochSeconds;
        private int count;

        private WindowCounter(long windowStartEpochSeconds) {
            this.windowStartEpochSeconds = windowStartEpochSeconds;
            this.count = 0;
        }
    }

    private static class RatePolicy {
        private final String keyPrefix;
        private final int maxRequests;
        private final int windowSeconds;

        private RatePolicy(String keyPrefix, int maxRequests, int windowSeconds) {
            this.keyPrefix = keyPrefix;
            this.maxRequests = maxRequests;
            this.windowSeconds = windowSeconds;
        }
    }

    private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        CachedBodyRequestWrapper(HttpServletRequest request, int maxBodyBytes) throws IOException, PayloadTooLargeException {
            super(request);
            this.body = readBodyWithLimit(request.getInputStream(), maxBodyBytes);
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(body);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        private static byte[] readBodyWithLimit(InputStream inputStream, int maxBodyBytes)
                throws IOException, PayloadTooLargeException {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int total = 0;
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    total += read;
                    if (total > maxBodyBytes) {
                        throw new PayloadTooLargeException();
                    }
                    output.write(buffer, 0, read);
                }

                return output.toByteArray();
            }
        }
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        CachedBodyServletInputStream(byte[] body) {
            this.inputStream = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
    }

    private static class PayloadTooLargeException extends Exception {
    }
}
