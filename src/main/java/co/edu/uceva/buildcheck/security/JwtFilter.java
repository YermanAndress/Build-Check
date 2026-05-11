package co.edu.uceva.buildcheck.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final Jwt jwt;

    public JwtFilter(Jwt jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        try {
            if (encabezado != null && encabezado.startsWith("Bearer ")) {
                String token = encabezado.substring(7);

                if (jwt.ValidarToken(token)) {
                    Claims correo = jwt.getClaims(token);
                    String rolProyecto = jwt.getRolProyecto(token);

                    // Handle null/empty rolProyecto with fallback authority
                    String authority;
                    if (rolProyecto != null && !rolProyecto.isEmpty()) {
                        authority = rolProyecto;
                    } else {
                        authority = "ROLE_AUTHENTICATED"; // Fallback for users in setup phase
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            correo,
                            null,
                            List.of(new SimpleGrantedAuthority(authority)));
                    auth.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                if (!jwt.esAccesToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Se requiere un access token");
                    return;
                }
                Claims correo = jwt.getClaims(token);
                String rol = jwt.getRolProyecto(token);
                String springRole = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(springRole);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(correo, null,
                        List.of(authority));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalido o expirado");
            return;
        }
    }
}
