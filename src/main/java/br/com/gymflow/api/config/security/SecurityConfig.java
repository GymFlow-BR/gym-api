package br.com.gymflow.api.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/api/organizations/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/exercises/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers("/api/workouts/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers("/api/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/students/**")
                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                        .requestMatchers(HttpMethod.POST, "/api/students/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.PATCH, "/api/students/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.DELETE, "/api/students/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.PUT, "/api/students/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}