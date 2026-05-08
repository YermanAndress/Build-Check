package co.edu.uceva.buildcheck.security;

import org.springframework.stereotype.Component;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class Jwt {
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.access-expiration}")
    private long ACCESS_EXPIRATION;

    @Value("${jwt.refresh-expiration}")
    private long REFRESH_EXPIRATION;

    private Key getLoginKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generarAccesToken(String correo, RolNombre rol){
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol.name());
        claims.put("type", "ACCESS");
        return Jwts.builder()
                .setSubject(correo)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(getLoginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarRefreshToken(String correo){
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

    public boolean ValidarToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getLoginKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getCorreo(String token){
        return getClaims(token).getSubject();
    }

    public String getRol(String token){
        return getClaims(token).get("rol", String.class);
    }

    public String getTipo(String token){
        return getClaims(token).get("type", String.class);
    }

    public boolean esAccesToken(String token){
        return "ACCESS".equals(getTipo(token));
    }

    public boolean esRefreshToken(String token){
        return "REFRESH".equals(getTipo(token));
    }
}
