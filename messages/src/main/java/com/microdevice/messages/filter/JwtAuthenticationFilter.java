package com.microdevice.messages.filter;

import com.microdevice.messages.service.JwtValidationService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that extracts a Bearer JWT from the {@code Authorization} header,
 * validates it via {@link JwtValidationService}, and populates the Spring Security
 * context with the authenticated principal and granted authorities.
 *
 * <p>Requests to the health endpoint, Swagger UI, and OpenAPI docs paths are skipped.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Service responsible for JWT signature verification and claims extraction. */
    private final JwtValidationService jwtValidationService;

    /**
     * Constructs the filter with the given JWT validation service.
     *
     * @param jwtValidationService the service used to validate JWT tokens
     */
    public JwtAuthenticationFilter(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    /**
     * Determines whether the filter should be skipped for the given request.
     * Skips the health endpoint, Swagger UI paths, and OpenAPI docs paths.
     *
     * @param request the incoming HTTP request
     * @return {@code true} if the request path should bypass JWT authentication
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/messages/health")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs");
    }

    /**
     * Extracts the Bearer token from the {@code Authorization} header, validates it,
     * and sets the Spring Security authentication context. If the token is missing or
     * invalid, the security context is cleared and the request proceeds unauthenticated.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtValidationService.validateToken(token);
                String username = claims.getSubject();

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = List.of();
                if (roles != null) {
                    authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                }

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
