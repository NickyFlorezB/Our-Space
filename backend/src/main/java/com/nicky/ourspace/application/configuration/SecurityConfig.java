package com.nicky.ourspace.application.configuration;

import java.util.List;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;

import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtAuthenticationFilter;
import com.nicky.ourspace.infrastructure.drivenadapters.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {

                JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

                return http
                                .cors(cors -> {
                                })
                                // Access tokens are sent in the Authorization header. Refresh-cookie
                                // endpoints enforce the configured Origin and SameSite policy.
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .securityContext(context -> context.securityContextRepository(
                                                new NullSecurityContextRepository()))
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)

                                .authorizeHttpRequests(auth -> auth
                                                .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                                                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                                                .requestMatchers("/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                                                .requestMatchers("/auth/me").authenticated()
                                                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(unauthorizedEntryPoint()))
                                .addFilterAfter(
                                                jwtFilter, LogoutFilter.class)
                                .build();
        }

        private AuthenticationEntryPoint unauthorizedEntryPoint() {
                return (request, response, exception) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        @Bean
        CorsConfigurationSource corsConfigurationSource(
                        @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") List<String> allowedOrigins) {
                CorsConfiguration configuration = new CorsConfiguration();
                List<String> origins = allowedOrigins.stream().map(String::trim).toList();
                if (origins.isEmpty() || origins.stream().anyMatch(String::isBlank) || origins.contains("*")) {
                        throw new IllegalStateException(
                                        "app.cors.allowed-origins must contain explicit origins and cannot contain '*'");
                }
                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
