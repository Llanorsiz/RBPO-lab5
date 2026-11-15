package kurban.softwareProducts.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long ACCESS_EXPIRATION_MS = 15 * 60 * 1000;
    private final long REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;

    public String generateAccessToken(String username, String role, Long userId){
        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .claim("role", role)
                .claim("id", userId)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(ACCESS_EXPIRATION_MS)))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username){
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(REFRESH_EXPIRATION_MS)))
                .signWith(key)
                .compact();
    }

    public Claims extract(String token, String expectedType){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getPayload();
            if(!expectedType.equals(claims.get("type"))){
                throw new JwtException("Некорректный тип токена");
            }
            return claims;
        }catch (ExpiredJwtException e){
            throw new JwtException("Срок действия токена истёк");
        }catch(Exception e){
            throw new JwtException("Некорректный токен");
        }
    }
}