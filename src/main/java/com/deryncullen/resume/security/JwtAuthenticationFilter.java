package com.deryncullen.resume.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("=== JWT Filter Processing ===");
        log.debug("Path: {}, Method: {}", request.getRequestURI(), request.getMethod());

        final String authHeader = request.getHeader("Authorization");

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token present - continuing without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token
            final String jwt = authHeader.substring(7);
            log.debug("JWT token found, length: {}", jwt.length());

            // Extract username from JWT
            final String userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username from JWT: {}", userEmail);

            // If username is present and user is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                log.debug("User details loaded for: {}", userEmail);

                // Validate token
                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    log.debug("JWT token is VALID for user: {}", userEmail);

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set in SecurityContext for user: {}", userEmail);
                } else {
                    log.warn("JWT token validation FAILED for user: {}", userEmail);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            // Token expired - continue without authentication (will be caught by security config)
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT signature is invalid: {}", e.getMessage());
            // Invalid signature - continue without authentication
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            // Malformed token - continue without authentication
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
            // Any other error - continue without authentication
        }

        filterChain.doFilter(request, response);
    }
}