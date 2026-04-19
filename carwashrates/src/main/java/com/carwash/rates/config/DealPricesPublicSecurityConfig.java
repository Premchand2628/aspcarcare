package com.carwash.rates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class DealPricesPublicSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain dealPricesPublicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/deal-prices/**", "/services/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}