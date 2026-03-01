package com.carwashcommon.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@EnableMethodSecurity
@ConditionalOnClass(HttpSecurity.class)
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "carwash.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityAutoConfig {

  @Bean
  @ConditionalOnMissingBean
  public JwtTokenService jwtTokenService(JwtProperties props) {
    return new JwtTokenService(props);
  }

  @Bean
  @ConditionalOnMissingBean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService tokenService) {
    return new JwtAuthenticationFilter(tokenService);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 JwtAuthenticationFilter jwtFilter) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers("/auth/**", "/otp/**", "/auth/password/forgot/send-otp").permitAll()
        .requestMatchers("/bookings/**").permitAll()
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/actuator/**").permitAll()
        .anyRequest().authenticated()
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
//package com.carwashcommon.security;
//
//import java.util.List;
//
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@AutoConfiguration
//@EnableMethodSecurity
//@ConditionalOnClass(HttpSecurity.class)
//@ConditionalOnProperty(prefix = "carwash.security", name = "enabled", havingValue = "true", matchIfMissing = true)
//public class SecurityAutoConfig {
//
//    private final JwtAuthenticationFilter jwtFilter;
//
//    public SecurityAutoConfig(JwtAuthenticationFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//    @Bean
//    @ConditionalOnMissingBean
//    public JwtTokenService jwtTokenService(JwtProperties props) {
//      return new JwtTokenService(props);
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService tokenService) {
//      return new JwtAuthenticationFilter(tokenService);
//    }
//
//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//            .cors(Customizer.withDefaults())
//            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//            .authorizeHttpRequests(auth -> auth
//
//                // ✅ preflight
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                // ✅ auth service endpoints (only for auth-user-service, harmless in others)
//                .requestMatchers("/auth/**").permitAll()
//
//                // ✅ swagger + actuator (optional)
//                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
//                .requestMatchers("/actuator/**").permitAll()
//
//                // ✅ static resources if this service serves UI
//                .requestMatchers(
//                    "/", "/index.html",
//                    "/*.html",
//                    "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.ico"
//                ).permitAll()
//
//                // ✅ PUBLIC endpoints across services (decide carefully)
//                // rates: you might want to allow rates read without login:
//                .requestMatchers(HttpMethod.GET, "/rates/**").permitAll()
//
//                // centres search & quotation read can be public if you want:
//                .requestMatchers(HttpMethod.GET, "/centres/**").permitAll()
//                .requestMatchers(HttpMethod.GET, "/quotations/**").permitAll()
//
//                // Everything else needs JWT
//                .anyRequest().authenticated()
//            )
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}
//
