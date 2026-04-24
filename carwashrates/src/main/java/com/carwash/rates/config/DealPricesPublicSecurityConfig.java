package com.carwash.rates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Public security chain for rates endpoints.
 *
 * Only /services/** (read-only centre/service catalogue) is exposed
 * unauthenticated. /deal-prices/** is now authenticated via the default
 * security chain (JwtAuthenticationFilter) because it leaks commercial
 * pricing data that should not be scrape-able by anonymous clients.
 */
@Configuration
public class DealPricesPublicSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain servicesPublicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/services/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}