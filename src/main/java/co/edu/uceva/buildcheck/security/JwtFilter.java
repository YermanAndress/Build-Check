package co.edu.uceva.buildcheck.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final Jwt jwt;

    public JwtFilter(Jwt jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        try {
            if (encabezado != null && encabezado.startsWith("Bearer ")) {
                String token = encabezado.substring(7);

                if (!jwt.ValidarToken(token)) {
                    throw new JwtException("Token invalido o expirado");
                }
                if (!jwt.esAccesToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Se requiere un access token");
                    return;
                }

                String correo = jwt.getCorreo(token);
                String rolProyecto = jwt.getRolProyecto(token);

                // Si no hay rol en el token (usuario sin proyecto), asignar un rol por defecto
                String springRole = (rolProyecto != null && !rolProyecto.isEmpty())
                        ? (rolProyecto.startsWith("ROLE_") ? rolProyecto : "ROLE_" + rolProyecto)
                        : "ROLE_AUTHENTICATED";

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(springRole);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        correo, null, List.of(authority));
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