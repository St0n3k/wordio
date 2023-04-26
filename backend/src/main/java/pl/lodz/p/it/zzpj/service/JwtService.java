package pl.lodz.p.it.zzpj.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.zzpj.entity.Account;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.expiration.time}")
    private Long jwtExpirationInMillis;

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateJWT(Account account) {
        return Jwts.builder()
            .setSubject(account.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMillis))
            .claim("role", Collections.singleton(account.getRole()))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    public Jws<Claims> parseJWT(String jwt) {
        return Jwts.parserBuilder()
            .setSigningKey(key).build()
            .parseClaimsJws(jwt);
    }


    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        return null;
    }

    public boolean validateToken(String jwt) {
        try {
            parseJWT(jwt);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }
}
