package com.deryncullen.resume.config;

import com.deryncullen.resume.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${security.permit-all:false}")
    private boolean permitAll;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== CONFIGURING SECURITY FILTER CHAIN ===");
        log.info("Permit All Mode: {}", permitAll);
        log.info("JWT Filter: {}", jwtAuthFilter.getClass().getSimpleName());
        log.info("Auth Provider: {}", authenticationProvider.getClass().getSimpleName());

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (permitAll) {
            // Test mode - permit all requests
            log.info("=== CONFIGURING TEST MODE - PERMIT ALL ===");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            // Production mode - JWT authentication
            log.info("=== CONFIGURING PRODUCTION MODE - JWT AUTHENTICATION ===");
            http.authorizeHttpRequests(auth -> {
                        log.info("=== CONFIGURING AUTHORIZATION RULES ===");
                        auth
                                // Public authentication endpoints (no token needed)
                                .requestMatchers("/auth/**").permitAll()

                                // Public profile endpoints - GET only (anyone can view profiles)
                                .requestMatchers(HttpMethod.GET, "/profiles/**").permitAll()

                                // Swagger/OpenAPI documentation
                                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()

                                // Actuator endpoints
                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                                // ALL OTHER REQUESTS require authentication
                                .anyRequest().authenticated();

                        log.info("=== AUTHORIZATION RULES CONFIGURED ===");
                        log.info("Public: /auth/**, GET /profiles/**, /swagger-ui/**, /actuator/**");
                        log.info("Protected: Everything else (POST/PUT/DELETE /profiles, etc.)");
                    })
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint((request, response, authException) -> {
                                log.debug("Authentication failed for: {} {}", request.getMethod(), request.getRequestURI());
                                log.debug("Reason: {}", authException.getMessage());
                                response.setContentType("application/json");
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" +
                                        authException.getMessage() + "\"}");
                            })
                    )
                    .authenticationProvider(authenticationProvider)
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        log.info("=== SECURITY CONFIGURATION COMPLETE ===");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:8080",
                "http://localhost:8081"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}