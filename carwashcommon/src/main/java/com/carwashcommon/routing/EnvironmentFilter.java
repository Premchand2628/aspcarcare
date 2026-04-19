package com.carwashcommon.routing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the {@code X-Env} header injected by Nginx and stores the
 * environment key in {@link EnvironmentContext} for the duration of
 * the request.  Nginx sets:
 * <ul>
 *   <li>{@code qa}   – for qa.aspcarcare.com / stg.aspcarcare.com</li>
 *   <li>{@code prod}  – for aspcarcare.com</li>
 * </ul>
 */
public class EnvironmentFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String env = request.getHeader("X-Env");
        if ("qa".equalsIgnoreCase(env)) {
            EnvironmentContext.set("qa");
        } else {
            EnvironmentContext.set("prod");
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            EnvironmentContext.clear();
        }
    }
}
