package com.bookbridge.config;

import com.bookbridge.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CorsFilter corsFilter, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.corsFilter = corsFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .addFilterBefore(corsFilter, org.springframework.web.filter.CorsFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll() // allow public GET
                .requestMatchers(HttpMethod.POST, "/api/books").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/books/request").authenticated()
                .requestMatchers("/api/login", "/api/register/**", "/api/password/reset", "/api/password/reset/complete").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}