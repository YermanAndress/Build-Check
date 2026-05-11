package co.edu.uceva.buildcheck.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uceva.buildcheck.security.JwtFilter;

import org.springframework.context.annotation.Configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(configurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/usuarios-service/login",
                                "/api/usuarios-service/public-key",
                                "/api/usuarios-service/usuarios")
                        .permitAll()
                        // Endpoints de proyectos - todos autenticados
                        .requestMatchers("/api/proyecto-service/proyectos/unirse").authenticated()
                        .requestMatchers("/api/proyecto-service/proyectos/usuario/**").authenticated()
                        .requestMatchers("/api/proyecto-service/proyectos/**").authenticated()
                        // Otros servicios
                        .requestMatchers("/api/materiales-service/**").authenticated()
                        .requestMatchers("/api/movimientos-service/**").authenticated()
                        .requestMatchers("/api/facturas-service/**").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource configurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Proyecto-Id"));
        config.setExposedHeaders(List.of("Authorization", "X-Proyecto-Id"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}