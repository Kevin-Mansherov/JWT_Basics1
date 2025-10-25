//package com.example.jwt_basics1.config;
//
//import com.example.jwt_basics1.service.CustomLogoutHandler;
//import com.example.jwt_basics1.service.CustomUserDetailsService;
//import com.example.jwt_basics1.service.TokenBlackListService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//
//import java.util.List;
//
//
//@Configuration
//@EnableWebSecurity(debug = true) // enable debug mode to see the security filter chain in action
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtUtil jwtUtil;
//    private final CustomUserDetailsService userDetailsService;
//    private final CustomLogoutHandler customLogoutHandler;
////    private final TokenBlackListService tokenBlackListService;
//
//    @Bean
//    public static PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        // we don't need csrf protection in jwt
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                // add cors corsConfigurer
//                .cors(cors -> {
//                    // register cors configuration source, React app is running on localhost:5173
//                    cors.configurationSource(request -> {
//                        var corsConfig = new CorsConfiguration();
//                        corsConfig.setAllowedOrigins(List.of("http://localhost:5173")); // vite dev server
//                        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                        corsConfig.setAllowedHeaders(List.of("*"));
//                        return corsConfig;
//                    });
//                })
//
//
//                // adding a custom JWT authentication filterד
//                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
//                        UsernamePasswordAuthenticationFilter.class)
//
//                // The SessionCreationPolicy.STATELESS setting means that the application will not create or use HTTP sessions.
//                // This is a common configuration in RESTful APIs, especially when using token-based authentication like JWT (JSON Web Token).
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//
//                // Configuring authorization for HTTP requests
//                .authorizeHttpRequests(auth -> auth
//                        //add the refresh token endpoint to be publicly accessible
//                        .requestMatchers("/api/refresh-token").permitAll()
//
//                        .requestMatchers("/api/login").permitAll()
//
//                        .requestMatchers("api/protected-message-admin").hasAnyRole("ADMIN")
//                        .requestMatchers("api/protected-message").hasAnyRole("USER", "ADMIN")
//
//                        .anyRequest().authenticated()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/api/logout")
//                        .logoutSuccessHandler(customLogoutHandler)
//                        .invalidateHttpSession(true)
//                        .clearAuthentication(true)
//                        .permitAll());
//
//        return http.build();
//    }
//
//}


package com.example.jwt_basics1.config;


import com.example.jwt_basics1.service.CustomLogoutHandler;
import com.example.jwt_basics1.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final CustomLogoutHandler customLogoutHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Allow CORS from your React dev server (http://localhost:5173)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("http://localhost:5173"));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))

                // Add the custom JWT authentication filter
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)

                // Stateless: no session stored server-side
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        // === Public endpoints ===
                        .requestMatchers("/api/login", "/api/register", "/api/refresh-token").permitAll()

                        // === Old endpoints kept for reference ===
                        .requestMatchers("/api/protected-message-admin").hasRole("ADMIN")
                        .requestMatchers("/api/protected-message").hasAnyRole("USER", "ADMIN")

                        // === New endpoints from the update ===
                        .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                        // === All other endpoints require authentication ===
                        .anyRequest().authenticated()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(customLogoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}
