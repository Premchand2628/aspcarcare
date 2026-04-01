package com.carwash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.auth.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.booking.url:http://localhost:8082}")
    private String bookingServiceUrl;

    @Value("${services.payment.url:http://localhost:8083}")
    private String paymentServiceUrl;

    @Value("${services.support.url:http://localhost:8084}")
    private String supportServiceUrl;

    @Value("${services.membership.url:http://localhost:8085}")
    private String membershipServiceUrl;

    @Value("${services.rates.url:http://localhost:8086}")
    private String ratesServiceUrl;

    @Value("${services.coupon.url:http://localhost:8088}")
    private String couponServiceUrl;

    @Value("${services.carwasher.url:http://localhost:8090}")
    private String carwasherServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-user-service", r -> r
                        .path("/users/**", "/auth/**", "/otp/**")
                        .uri(authServiceUrl))
                .route("booking-service", r -> r
                        .path("/bookings/**", "/quotations/**", "/centres/**")
                        .uri(bookingServiceUrl))
                .route("payment-service", r -> r
                        .path("/payments/**")
                        .uri(paymentServiceUrl))
                .route("support-chat-service", r -> r
                        .path("/support/**", "/chat/**", "/agent-chat/**", "/tickets/**")
                        .uri(supportServiceUrl))
                .route("membership-service", r -> r
                        .path("/memberships/**")
                        .uri(membershipServiceUrl))
                .route("carwash-rates-service", r -> r
                        .path("/rates/**", "/carwashrates/**", "/api/deal-prices/**")
                        .uri(ratesServiceUrl))
                .route("coupon-service", r -> r
                        .path("/coupons/**")
                        .uri(couponServiceUrl))
                .route("carwasher-service", r -> r
                        .path("/api/auth/**", "/api/orders/**", "/api/washer/**")
                        .uri(carwasherServiceUrl))
                .build();
    }
}
