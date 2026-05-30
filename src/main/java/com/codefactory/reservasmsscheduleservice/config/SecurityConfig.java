package com.codefactory.reservasmsscheduleservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.codefactory.reservasmsscheduleservice.security.JwtAccessDeniedHandler;
import com.codefactory.reservasmsscheduleservice.security.JwtAuthenticationEntryPoint;
import com.codefactory.reservasmsscheduleservice.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // Health check público
                        .requestMatchers("/api/", "/api/version").permitAll()
                        // Swagger/OpenAPI - Documentación pública
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/configuration/**").permitAll()
                        // Actuator endpoints para Prometheus (no exponer en prod)
                        .requestMatchers("/actuator/**").permitAll()

                        // Endpoint público para empleados activos
                        .requestMatchers("/api/schedule/employees/active").permitAll()
                        // Endpoints públicos para integración con Reservation Service
                        .requestMatchers("/api/schedule/employees/*/active").permitAll()
                        .requestMatchers("/api/schedule/employees/*/provider").permitAll()
                        .requestMatchers("/api/schedule/employees/*/info").permitAll()
                        // Endpoints internos para comunicación entre microservicios (Reservation -> Schedule)
                        // La validación de permisos se hace en el servicio, no en el controlador
                        .requestMatchers("/api/schedule/schedule-blocks/reservation").permitAll()
                        .requestMatchers("/api/schedule/schedule-blocks/reservation/*").permitAll()
                        // Endpoint público para empleados activos de un servicio
                        .requestMatchers("/api/schedule/employee-services/service/*/active").permitAll()
                        // Employee Service Offering endpoints requieren autenticación
                        // (el controlador maneja la autorización por rol con @PreAuthorize)
                        .requestMatchers("/api/schedule/employee-services/**").authenticated()
                        // Work Schedule endpoints requieren autenticación
                        // (el controlador maneja la autorización por rol con @PreAuthorize)
                        .requestMatchers("/api/schedule/work-schedules/**").authenticated()
                        // Schedule Block endpoints requieren autenticación
                        // (el controlador maneja la autorización por rol con @PreAuthorize)
                        .requestMatchers("/api/schedule/schedule-blocks/**").authenticated()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}