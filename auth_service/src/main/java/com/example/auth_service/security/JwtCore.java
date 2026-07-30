package com.example.auth_service.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtCore {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.time}")
    private Long timeExpire;

    private SecretKey secretKey;

    private SecretKey getSecretKey() {
        if(secretKey == null) {
            byte[] byteSecretKey = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            secretKey = Keys.hmacShaKeyFor(byteSecretKey);
        }
        return secretKey;
    }


    public String jwtGenerator(UserDetails userDetails){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + timeExpire);
        Map<String, Object> claims = new HashMap<>();
        UserDetailsImpl user = (UserDetailsImpl) userDetails;
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        List<String> roles = user.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expireDate)
                .and()
                .signWith(getSecretKey())
                .compact();
    }

    public String extractUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } catch (SignatureException ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
        } catch (Exception ex) {
            log.info("------->>>>>>  {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
        return false;
    }
}