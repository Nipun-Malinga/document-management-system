package com.nipun.system.shared.services;

import com.nipun.system.shared.config.JwtConfig;
import com.nipun.system.shared.entities.Jwt;
import com.nipun.system.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class JwtService {

    private final JwtConfig config;

    public Jwt generateAccessToken(User user) {
        return generateToken(user, config.getAccessTokenExpiration());
    }

    public Jwt generateRefreshToken(User user) {
        return generateToken(user, config.getRefreshTokenExpiration());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(config.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Jwt parseToken(String token) {
        try {
            return new Jwt(config.getSecretKey(), getClaims(token));
        } catch (JwtException ex) {
            return null;
        }
    }

    private Jwt generateToken(User user, Long tokenExpiration) {
         var claims = Jwts.claims()
                 .subject(user.getId().toString())
                 .add("email", user.getEmail())
                 .add("username",  user.getUsername())
                 .add("role", user.getRole())
                 .issuedAt(new Date())
                 .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                 .build();

         return new Jwt(config.getSecretKey(), claims);
    }
}
