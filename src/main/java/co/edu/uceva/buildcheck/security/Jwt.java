package co.edu.uceva.buildcheck.security;

import org.springframework.stereotype.Component;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class Jwt {
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION;

    @Value("${jwt.expiration}")
    private long EXPIRATION;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Key getLoginKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generarRefreshToken(String correo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return Jwts.builder()
                .setSubject(correo)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(getLoginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarToken(String correo, Long proyectoId, RolNombre rolProyecto) {
        Long usuarioId = getUsuarioId(correo);
        var builder = Jwts.builder()
                .setSubject(correo)
                .claim("usuarioId", usuarioId)
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION));

        if (proyectoId != null) {
            builder.claim("proyectoId", proyectoId);
        }
        if (rolProyecto != null) {
            builder.claim("rolProyecto", rolProyecto.name());
        }

        return builder.signWith(getLoginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean ValidarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getLoginKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getCorreo(String token) {
        return getClaims(token).getSubject();
    }

    public Long getProyectoId(String token) {
        Object proyectoId = Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("proyectoId");

        if (proyectoId instanceof Number) {
            return ((Number) proyectoId).longValue();
        }
        return null;
    }

    public String getRolProyecto(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rolProyecto", String.class);
    }

    public Long getUsuarioId(String correo) {
        var usuario = usuarioRepository.findByCorreo(correo);
        if (usuario.isPresent()) {
            return usuario.get().getId();
        }
        throw new IllegalArgumentException("Usuario no encontrado con correo: " + correo);
    }

    public Long getUsuarioIdFromToken(String token) {
        Object usuarioId = Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("usuarioId");

        if (usuarioId instanceof Number) {
            return ((Number) usuarioId).longValue();
        }
        return null;
    }

    public String getTipo(String token) {
        return getClaims(token).get("type", String.class);
    }

    public boolean esAccesToken(String token) {
        return "ACCESS".equals(getTipo(token));
    }

    public boolean esRefreshToken(String token) {
        return "REFRESH".equals(getTipo(token));
    }

}
