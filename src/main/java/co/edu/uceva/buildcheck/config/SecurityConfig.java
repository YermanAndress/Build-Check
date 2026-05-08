package co.edu.uceva.buildcheck.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uceva.buildcheck.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;

import org.springframework.context.annotation.Bean;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter){
        this.jwtFilter = jwtFilter;
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(configurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/usuarios-service/login",
                    "/api/usuarios-service/refresh",
                    "/api/usuarios-service/public-key",
                    "/api/usuarios-service/usuarios"                    
                ).permitAll()
                // Admin tendra acceso a todo
                .requestMatchers("/api/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "api/materiales-service/**").hasAnyRole("ALMACENISTA", "DIRECTOR_OBRA", "RESIDENTE")
                .requestMatchers(HttpMethod.POST, "/api/materiales-service/**").hasRole("ALMACENISTA")
                .requestMatchers(HttpMethod.PUT, "/api/materiales-service/**").hasRole("ALMACENISTA")
                .requestMatchers(HttpMethod.GET, "api/facturas-service/**").hasRole("ALMACENISTA")
                .requestMatchers(HttpMethod.POST, "api/facturas-service/**").hasRole("ALMACENISTA")
                .requestMatchers("/api/proyectos-service/**").hasRole("DIRECTOR_OBRA")
                .requestMatchers("api/movimientos-service/**").hasRole("RESIDENTE")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado")
                    )
                )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource configurationSource(){
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}