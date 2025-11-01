package com.deryncullen.resume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("!test") // Only active when NOT in test profile
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - anyone can read profiles
                        .requestMatchers(HttpMethod.GET, "/api/profiles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profiles/active").permitAll()

                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/api/swagger-ui/**").permitAll()
                        .requestMatchers("/api/api-docs/**").permitAll()
                        .requestMatchers("/api/swagger-ui.html").permitAll()

                        // Actuator endpoints
                        .requestMatchers("/api/actuator/health").permitAll()
                        .requestMatchers("/api/actuator/info").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {}); // Basic auth for now, will replace with JWT

        return http.build();
    }

    @Bean
    @Profile("test") // Only active in test profile
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // Next.js development
                "http://localhost:3001",  // Alternative port
                "http://localhost:8080"   // Local development
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}