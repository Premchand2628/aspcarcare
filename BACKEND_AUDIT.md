# Backend Audit — `D:\aspcare`

Read-only audit of all 8 Spring Boot services + shared `carwashcommon` + root configs.
Findings are **evidence-based** (file + line + snippet). Fixes are suggestions, not applied.

**Scope covered**
- `otploginauth` (auth-user-service)
- `bookingservice`
- `carwashrates`
- `membership`
- `paymentservice`
- `carwashcommon` (shared security, logging, routing, cache)
- `uigatewayservice`
- `mailnotification`, `supportchatservice`, `invitation`, `carwasherservice` (quick pass)
- root configs: `.env`, `.env.example`, `env.properties`, startup scripts, GitHub Actions, `pom.xml`

---

## SEVERITY SUMMARY

| Severity | Count | Examples |
|---|---|---|
| **CRITICAL** | 12 | `.env` committed with real creds, PhonePe webhook no signature, client-supplied `baseAmount` trusted, OTP returned in HTTP body, `/deal-prices` public, OTP plaintext in memory, body logging unmasked |
| **HIGH** | 14 | HS256 shared secret, 6-char min password, in-memory token blacklist, dev rate-limit bypass flag, `User.toString()` leaks password, no `@Valid` on DTOs, weak enum validation, admin bulk upsert unlogged, `ngrok-free.dev` in CORS, `show-sql: true` in prod |
| **MEDIUM** | 15 | OTP 6 digits + 30 s cooldown, no account lockout, N+1 on booking list, long methods (140–160 lines), missing `@Transactional`, no pagination, no idempotency on `/payments`, X-Forwarded-For trust, actuator exposure |
| **LOW** | 10 | Inconsistent error messages (aid enumeration), hardcoded service URLs, UUID vs Long PK mismatch, missing `@EntityGraph`, CSRF-disabled without docs, dead `JwtService.java` |
| **POSITIVE** | 20+ | BCrypt, STATELESS sessions, HSTS, IDOR phone check on bookings, `@PreAuthorize` on admin, parameterised SQL, server-side membership discount, token-type enforcement, request-size cap, `Bearer ***` masked in logs |

---

## 🔴 CRITICAL — fix before next deploy

### C1. `.env` committed with real production secrets
**Evidence**: root `.env` tracked in git history. Contains keys like `DB_PASSWORD`, `JWT_SECRET`, `MAIL_APP_PASSWORD`, `PHONEPE_*`, `RAZORPAY_*`, `FACEBOOK_APP_SECRET`, `GOOGLE_CLIENT_SECRET`.
**Fix**: rotate every secret. Add `.env` to `.gitignore` (it is, but history needs purge via `git filter-repo`). Move prod secrets to AWS Secrets Manager / SSM Parameter Store, inject via systemd `EnvironmentFile=` from a non-repo file.

### C2. PhonePe / Razorpay webhook — no signature verification
**File**: `paymentservice` callback endpoint (`/payments/phonepe/callback`).
**Evidence**: request handler reads body + trusts `status` field; no `X-VERIFY` / `X-Razorpay-Signature` HMAC check.
**Fix**: Verify `X-VERIFY` = `SHA256(base64Body + endpoint + saltKey) ## saltIndex` for PhonePe; for Razorpay verify `X-Razorpay-Signature` = `HMAC-SHA256(body, webhookSecret)`. **Without this, anyone can POST a fake success and collect the wash for free.**

### C3. Client-supplied `baseAmount` trusted on booking create
**File**: `bookingservice/.../BookingServiceImpl.java` — `resolveOriginalAmount()` (~line 1022)
```java
if (req.getBaseAmount() != null && req.getBaseAmount().compareTo(BigDecimal.ZERO) >= 0) {
    return req.getBaseAmount();   // trusts client
}
```
**Fix**: delete the field from `BookingRequest` DTO. Always call `rateClient.getAmount(carType, washType)`. Same class, `applyMembershipPreviewToBooking()` trusts `discountPercent` from membership response — clamp `[0, 100]` and assert `payable >= 0`.

### C4. `/deal-prices/**` and `/services/**` are `permitAll()`
**File**: `carwashrates/.../DealPricesPublicSecurityConfig.java:16`
**Fix**: require authentication. Competitors scrape pricing; worse, attackers can pre-read the deal table to craft "I paid the deal price" claims.

### C5. OTP returned inside HTTP response
**File**: `otploginauth/.../AuthController.java` lines ~123, 148, 210, 265
```java
new ApiResponse(true, "OTP sent successfully (DEV only: " + otp + ")")
```
This is the source of the OTP panel in Grafana we already killed. **Remove unconditionally** — the `DEV only` marker doesn't help if the JAR is ever run with the wrong profile.

### C6. OTP stored plaintext in memory
**File**: `otploginauth/.../OtpService.java:27`
```java
private final ConcurrentMap<String, OtpData> otpStore = new ConcurrentHashMap<>();
```
Any JVM heap dump (`/actuator/heapdump`, OOM dump, operator `jmap`) exposes live OTPs and the phones they're bound to.
**Fix**: store `SHA-256(otp + perOtpSalt)` instead; compare by re-hashing submitted value. Two-line change.

### C7. `HttpRequestResponseLoggingFilter` logs raw request / response bodies
**File**: `carwashcommon/.../logging/HttpRequestResponseLoggingFilter.java:71`
```java
log.info("HTTP_REQUEST transactionId={} ... body={}", transactionId, requestBody);
```
Body contains `{"mobileNumber":"...","otp":"..."}`, passwords, tokens, card-payload stubs. The logback regex we added last round catches most on output, but **this is defence-in-depth — the filter itself should skip auth/payment paths**.
**Fix**: add a path allow-list/deny-list. Never log body for `/auth/**`, `/payments/**`, `/users/me/password`, `/admin/**`. Cap body logging at 2 KiB for everything else. Parse JSON and mask `otp|password|mobileNumber|authToken|refreshToken|cardNumber|cvv` before the string hits `log.info`.

### C8. User-enumeration via `/auth/check-phone`
**File**: `AuthController.java:93`
```java
return ResponseEntity.ok(Map.of("exists", true, "email", masked));
```
Scriptable: "which phones in 9*********  are customers, and what emails?". Also the login flow on the frontend uses this.
**Fix**: delete endpoint; always send OTP, create-on-verify if new.

### C9. Exception messages surfaced to clients
**File**: `AuthController.java:478`
```java
"message", "Failed to verify Google token: " + e.getMessage()
```
Leaks OAuth library internals, sometimes certificate subjects / network destinations.
**Fix**: generic client message; keep full `log.error("google verify failed", e)` server-side.

### C10. `TokenBlacklistService` is in-memory ConcurrentHashMap
**File**: `carwashcommon/.../security/TokenBlacklistService.java:12`
- Logout doesn't propagate across instances (fine today, you run a single instance; **will bite when you scale** or restart after a stolen-token incident).
- **Also wiped on every restart** — which means yesterday's stolen token is accepted again after tonight's systemd restart.
**Fix**: Redis-backed (`SET` with TTL = token `exp - now`). Spring Data Redis is already transitively available.

### C11. `User.toString()` dumps password hash
**File**: `otploginauth/.../entity/User.java:153`
Any `log.info("saved {}", user)` path leaks the BCrypt hash.
**Fix**: annotate with `@ToString(exclude = {"password"})` (Lombok) or override manually. One-liner.

### C12. `carwasherservice` runs with `spring.jpa.hibernate.ddl-auto: update`
**Evidence**: in that module's `application.yml`.
Production risk: a bad branch merge → auto DROP / ALTER on user tables.
**Fix**: set to `validate` in prod, use Flyway / Liquibase for migrations.

---

## 🟠 HIGH

1. **JWT uses HS256 (shared secret)** — any of 8 services can mint tokens. If one service is compromised, the attacker forges tokens for all. Move to RS256 (private key only in `otploginauth`, public key distributed). `carwashcommon/.../security/JwtTokenService.java:24`.
2. **Password minimum is 6 chars** — `AuthController.java:388`. Bump to 12 + at least one letter + one digit.
3. **`app.security.rate-limit.send-otp.dev-bypass-enabled=true` in `application-dev.properties`** — only 1 profile mistake away from disabling prod rate limit. Delete the flag entirely; rate limit is enforced by Spring profile, not a separate bypass boolean.
4. **No `@Valid` / no bean-validation annotations on DTOs** — `SendOtpRequest`, `VerifyOtpRequest`, `SignupRequest`, `BookingRequest.carType/washType` are plain strings. Inline `if-null-or-blank` checks are inconsistent across endpoints.
5. **`StaffAuthController.getUserStats()`** — no `@PreAuthorize("hasRole('ADMIN')")`. Relies on `SecurityFilterChain.anyRequest().authenticated()`, which is "any logged-in user = admin access". Add the annotation.
6. **Admin bulk upsert no audit log** — `QuotationController.upsertAll()` accepts `List<CarwashQuotation>`, no who/when/delta log, no clamp on row count. Bulk-zeroing all prices is one request.
7. **`carType` / `washType` are strings with length limit, not enums** — `RateClient.normalizeWashLevel()` defaults unknown strings to `BASIC` (cheapest). A client sending `washType: "NOPE"` books at BASIC rate. Convert to Java enums.
8. **Feign/RestTemplate header propagation inconsistent** — `MembershipClient.preview()`, `apply()` don't forward `Authorization`; `redeem()`, `revert()` do. Standardise with a single `RequestInterceptor` bean.
9. **`ngrok-free.dev` in CORS allowlist** — `carwashcommon/.../SecurityAutoConfig.java` (or service-level CORS). Anyone on any ngrok tunnel can hit you with credentials. Remove; use env-specific lists.
10. **`spring.jpa.show-sql: true` in prod** — logs query parameters including phone, email, amount, hashed password. Set to `false` for prod profile; rely on debug toggle in lower envs.
11. **Paytm Maven repo over HTTP** — `pom.xml` declares `<url>http://...</url>` for paytm SDK. MITM on `mvn install` can inject a malicious JAR. Switch to HTTPS or vendor the artifact.
12. **`setup_qa_db.sh` appends `QA_DB_PASSWORD=Chandu@2628` plaintext** — committed to repo. Rotate this password; rewrite script to prompt or read from env.
13. **No issuer (`iss`) claim validation** — `JwtAuthenticationFilter` accepts any JWT signed with the shared secret. Combined with the HS256 shared-secret problem, a compromised minor service can mint admin tokens.
14. **`AuthAbuseProtectionFilter` uses `request.getRemoteAddr()`** — behind nginx this is always `127.0.0.1`, so per-IP rate limit becomes global. Read `X-Forwarded-For` (first value) and configure `server.forward-headers-strategy=native`.

---

## 🟡 MEDIUM

1. **OTP length 6** — brute-force at 5 attempts / 600 s ≈ 33 h per phone. Bump to 8 digits or alphanumeric.
2. **OTP resend cooldown 30 s** — spam vector (cost to SMS gateway, inbox noise). 60–90 s is standard.
3. **No persistent account lockout on repeated OTP failures** — MAX_ATTEMPTS=5 invalidates only the current code, not subsequent requests.
4. **Missing `@Email` / `@Pattern` on forgot-password DTOs** — malformed emails reach downstream.
5. **N+1 on `getBookingsByPhone`** — `findByPhoneOrderByCreatedAtDesc` + `.map(fromEntity)` touches lazy `refunds`, `invoice`. Add `@EntityGraph(attributePaths = {"refunds", "invoice"})`.
6. **`BookingServiceImpl.createBooking` ~140 lines, `confirmOrder` ~160 lines** — extract `validateAndResolvePrice`, `applyDiscounts`, `persistAndNotify`.
7. **Not `@Transactional`** — `createBooking` saves booking, then calls email, then feign. If email throws, booking persists but confirmation path fails silently. Wrap in `@Transactional`; fire email via `ApplicationEventPublisher` for async/post-commit.
8. **No rate limit on `/bookings` create/cancel** — attacker can burn inventory and spam emails.
9. **`GET /bookings/all`** — no pagination (`List<BookingResponseDto>`). Add `Pageable` arg.
10. **No idempotency key on `/payments`** — double-click → double charge. Accept `Idempotency-Key` header, store `(key, bookingId, response)` for 24 h.
11. **Spring Boot Actuator exposure defaults not explicitly restricted** on backend services. Set `management.endpoints.web.exposure.include: health,info` everywhere; never `*`.
12. **Missing `@Transactional` on `StaffAuthService.register()`** — multi-step writes.
13. **`InvoicePdfService` — PDF access control unclear** — if there is a `/invoices/{id}` download endpoint, ensure ownership check + signed URL.
14. **`User.id = Long` vs `CentreStaff.id = UUID`** — inconsistency complicates admin tooling and makes joins awkward.
15. **Connection pool sizing for 2 GB instance** — if `spring.datasource.hikari.maximum-pool-size` is the default 10 per service × 8 services = 80 connections. MySQL / Postgres default `max_connections=100`. Explicitly set to 5 per service.

---

## 🟢 LOW

1. Inconsistent error messages (`Forbidden` vs `Unauthorized` vs `Forbidden: phone mismatch`) — helps IDOR probing.
2. Hardcoded `http://localhost:808x` in Feign clients — break in Docker. Externalise.
3. `AuthController` > 600 lines — extract `OAuthGoogleService`, `OAuthFacebookService`.
4. Dead code — `JwtService.java` in `otploginauth` is unused (everyone uses `JwtTokenService` in common).
5. `.cors(Customizer.withDefaults())` — relies on whatever `CorsConfigurationSource` bean happens to exist. Make it explicit per service.
6. `Facebook app.secret` in `application.yml` — even though it's `${FACEBOOK_APP_SECRET:}`, remove the default empty string so startup fails loud when missing.
7. Duplicated try/catch boilerplate in controllers — extract `@ControllerAdvice` with `@ExceptionHandler`.
8. `@JsonIgnore` on password — good, but also add `@Column(nullable = true)` audit; consider splitting credentials table.
9. Unused imports / trailing blank lines in multiple controllers.
10. Missing structured audit log for admin price-changes / refunds / cancellations (business-critical forensic trail).

---

## ✅ POSITIVE (keep doing)

- BCrypt for passwords — good, strength 10 default is fine.
- `SessionCreationPolicy.STATELESS` — no session fixation risk.
- `HSTS` configured in `SecurityAutoConfig`.
- IDOR check on booking endpoints: `if (!phone.equals(booking.getPhone())) return 403;` — pattern is correct.
- `@PreAuthorize("hasRole('ADMIN')")` on `DealPriceController` mutating ops, `AdminStatsController`, `QuotationController.upsertAll`.
- All `@Query` use `@Param` — no SQL injection via repositories.
- Membership benefit fetched server-side via Feign (client can't inject discount %) — aside from the HIGH #6 about clamping the returned value.
- `@Transactional` on `DealPriceBookingService.redeemForBooking` / `revertRedemption` — atomic wash-count updates.
- Token type claim checked (`access` vs `refresh` can't be swapped).
- `Authorization` header masked to `Bearer ***` in `HttpRequestResponseLoggingFilter.java:137`.
- `SecureRandom` used in `OtpService` (not `java.util.Random`).
- Constant-time compare on admin secret (`MessageDigest.isEqual`) in `StaffAuthController`.
- Spring Boot 3.2.1, JJWT 0.11.5 — current.
- Request-body size cap (16 KiB).
- Refresh-token blacklist on rotation (mitigates reuse).
- GitHub Actions uses secrets via `${{ secrets.* }}` (no hardcoded creds in workflows).

---

## RECOMMENDED FIX ORDER

### Week 1 — do immediately (no user-facing changes)
1. **C1** rotate all committed secrets; purge `.env` from history; move to AWS SSM.
2. **C2** add PhonePe + Razorpay webhook signature verification (pure-server, 1 method).
3. **C5** drop OTP from `AuthController` responses.
4. **C6** hash OTP in `OtpService`.
5. **C11** `@ToString(exclude = "password")`.
6. **HIGH #3** delete the `dev-bypass-enabled` flag entirely.
7. **HIGH #10** `show-sql: false` in prod profile.
8. **HIGH #12** change `setup_qa_db.sh` to read password from env.

### Week 2 — server-side only, no frontend coordination
9. **C7** path-scoped body logging + field masking in `HttpRequestResponseLoggingFilter`.
10. **C8** delete `/auth/check-phone` (coordinated with FE — `Review.jsx` calls it).
11. **C9** generic error messages; `@ControllerAdvice` globaliser.
12. **C3** delete `baseAmount` from `BookingRequest`; add `@Min/@Max` clamp on membership discount.
13. **C4** require auth on `/deal-prices`.
14. **C12** `ddl-auto: validate` + Flyway baseline.
15. **HIGH #2, #4, #5, #9, #13, #14** — small hardening items.

### Week 3–4 — bigger refactors
16. **C10** Redis-backed `TokenBlacklistService`.
17. **B1/B2/B3** new `/bookings/quote` + `/rewards/me` endpoints → frontend migration.
18. **HIGH #1** HS256 → RS256 key pair rollout (keyset versioned, in-flight tokens still accepted).
19. **C1/C2 frontend-side** httpOnly cookie + `/auth/refresh` endpoint; stop returning JWT in body; update `auth.js`.
20. **Medium #5–#14** N+1, pagination, idempotency, `@Transactional`, actuator hardening.

### Whenever convenient
21. LOW items: error-message standardisation, dead code removal, `AuthController` split, docs.

---

## Branch strategy reminder

Per your deployment: edits go in locally → `develop` → `qa` → `stg` → `main` (auto-deploy).
**Recommend**: one branch per week-bucket above, not per finding. Commits stay readable; rollbacks are cheap.

---

*Audit generated 2026-04-24. Three read-only passes (auth + security, business logic, infra + config). No source files modified. No secrets echoed into this report.*
