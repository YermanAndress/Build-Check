package co.edu.uceva.buildcheck.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uceva.buildcheck.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(configurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                "/api/usuarios-service/login",
                                "/api/usuarios-service/refresh",
                                "/api/usuarios-service/public-key")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios-service/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios-service/usuarios").permitAll()

                        // Solo ADMIN para todo lo demás de usuarios-service
                        .requestMatchers("/api/usuarios-service/**").hasAnyRole("OWNER", "ADMIN")

                        // Materiales
                        .requestMatchers(HttpMethod.GET, "/api/materiales-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA", "DIRECTOR_OBRA", "RESIDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/materiales-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/materiales-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")
                        .requestMatchers(HttpMethod.DELETE, "/api/materiales-service/**")
                        .hasAnyRole("OWNER", "ADMIN")

                        // Movimientos
                        .requestMatchers(HttpMethod.GET, "/api/movimientos-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA", "DIRECTOR_OBRA", "RESIDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/movimientos-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA", "RESIDENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/movimientos-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")
                        .requestMatchers(HttpMethod.DELETE, "/api/movimientos-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")

                        // Facturas
                        .requestMatchers(HttpMethod.GET, "/api/facturas-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA", "DIRECTOR_OBRA")
                        .requestMatchers(HttpMethod.POST, "/api/facturas-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/facturas-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA")
                        .requestMatchers(HttpMethod.DELETE, "/api/facturas-service/**")
                        .hasAnyRole("OWNER", "ADMIN")

                        // Proyecto
                        .requestMatchers(HttpMethod.GET, "/api/proyecto-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "ALMACENISTA", "DIRECTOR_OBRA", "RESIDENTE")
                        .requestMatchers(HttpMethod.POST, "/api/proyecto-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "DIRECTOR_OBRA")
                        .requestMatchers(HttpMethod.PUT, "/api/proyecto-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "DIRECTOR_OBRA")
                        .requestMatchers(HttpMethod.DELETE, "/api/proyecto-service/**")
                        .hasAnyRole("OWNER", "ADMIN", "DIRECTOR_OBRA")
                        .requestMatchers("/api/proyecto-service/proyectos/unirse")
                        .hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/api/proyecto-service/proyectos/usuario/**")
                        .hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers("/api/proyecto-service/proyectos/**")
                        .hasAnyRole("OWNER", "ADMIN")

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> response
                                .sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response
                                .sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource configurationSource() {
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