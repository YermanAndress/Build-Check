package co.edu.uceva.buildcheck.security;

import org.springframework.stereotype.Component;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class Jwt {
    @Value("${jwt.secret}")
    private String SECRET;
    @Value("${jwt.expiration}")
    private long EXPIRATION;

    private Key getLoginKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generarToken(String correo, RolNombre rol){
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
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

    public String getCorreo(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getLoginKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
