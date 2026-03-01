	package com.carwashcommon.security;
	
	import io.jsonwebtoken.Claims;
	import io.jsonwebtoken.Jws;
	import io.jsonwebtoken.JwtException;
	import jakarta.servlet.FilterChain;
	import jakarta.servlet.ServletException;
	import jakarta.servlet.http.HttpServletRequest;
	import jakarta.servlet.http.HttpServletResponse;
	import org.springframework.http.HttpHeaders;
	import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
	import org.springframework.security.core.authority.SimpleGrantedAuthority;
	import org.springframework.security.core.context.SecurityContextHolder;
	import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
	import org.springframework.web.filter.OncePerRequestFilter;
	
	import java.io.IOException;
	import java.util.ArrayList;
	import java.util.List;
	
	public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	    private final JwtTokenService tokenService;
	
	    public JwtAuthenticationFilter(JwtTokenService tokenService) {
	        this.tokenService = tokenService;
	    }
	
	    @Override
	    protected boolean shouldNotFilter(HttpServletRequest request) {
	        String path = request.getServletPath();
	        return path.startsWith("/auth/")
	                || path.startsWith("/otp/")
	                || path.startsWith("/swagger-ui")
	                || path.startsWith("/v3/api-docs")
	                || path.startsWith("/actuator");
	    }
	
	    @Override
	    protected void doFilterInternal(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    FilterChain filterChain) throws ServletException, IOException {
	
	        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
	
	        if (auth != null && auth.startsWith("Bearer ")) {
	            String token = auth.substring(7).trim();
	
	            try {
	                Jws<Claims> jws = tokenService.parseAndValidate(token);
	                Claims claims = jws.getBody();
	
	                String phone = claims.getSubject();
	
	                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
	                Object rolesObj = claims.get("roles");
	
	                if (rolesObj instanceof List<?> list) {
	                    for (Object r : list) {
	                        if (r != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
	                    }
	                } else if (rolesObj instanceof String s && !s.isBlank()) {
	                    for (String r : s.split(",")) {
	                        if (!r.isBlank()) authorities.add(new SimpleGrantedAuthority("ROLE_" + r.trim()));
	                    }
	                } else {
	                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
	                }
	
	                JwtUserPrincipal principal = new JwtUserPrincipal(phone, claims);
	
	                UsernamePasswordAuthenticationToken authentication =
	                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
	
	                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	
	                if (SecurityContextHolder.getContext().getAuthentication() == null) {
	                    SecurityContextHolder.getContext().setAuthentication(authentication);
	                }
	
	            } catch (JwtException ex) {
	                SecurityContextHolder.clearContext();
	                // optional: response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); return;
	            }
	        }
	
	        filterChain.doFilter(request, response);
	    }
	}
