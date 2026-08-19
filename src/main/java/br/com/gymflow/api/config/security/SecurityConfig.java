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
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.PATCH, "/api/auth/change-password")
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/students/*/workouts/current/exercises/*/complete",
                                "/api/students/*/workouts/current/exercises/*/uncomplete",
                                "/api/students/*/workouts/*/exercises/*/complete",
                                "/api/students/*/workouts/*/exercises/*/uncomplete"
                        )
                        .hasRole("STUDENT")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/students/*/workouts/current/progress"
                        )
                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")

                        .requestMatchers("/api/organizations/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/exercises/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers("/api/workouts/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/by-organization/*/by-role"
                        )
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/users/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.DELETE, "/api/users/**")
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