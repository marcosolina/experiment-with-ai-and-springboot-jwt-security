package com.microdevice.supervisor.filter;

import com.microdevice.supervisor.service.JwtService;
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
 * Servlet filter that extracts and validates JWT Bearer tokens from incoming requests.
 *
 * <p>When a valid token is found in the {@code Authorization} header, the filter parses
 * the claims, extracts the username and roles, and populates the Spring Security context
 * with an authenticated principal. Requests to authentication and Swagger UI paths are
 * skipped.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Service used to validate JWT tokens and extract claims. */
    private final JwtService jwtService;

    /**
     * Constructs the filter with the required JWT service.
     *
     * @param jwtService the service for JWT token validation
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Determines whether this filter should be skipped for the given request.
     *
     * <p>Authentication endpoints ({@code /api/auth/**}), Swagger UI, and OpenAPI docs
     * paths are excluded from filtering.</p>
     *
     * @param request the incoming HTTP request
     * @return {@code true} if the filter should be skipped, {@code false} otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs");
    }

    /**
     * Processes the request by extracting the Bearer token, validating it, and setting
     * the authentication in the security context.
     *
     * <p>If the token is missing or invalid, the security context is cleared and the
     * request proceeds without authentication.</p>
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
                Claims claims = jwtService.validateToken(token);
                String username = claims.getSubject();

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();

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
