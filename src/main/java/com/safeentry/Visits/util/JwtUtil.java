package com.safeentry.Visits.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // Pode ser necessário se você for assinar tokens aqui, mas para validação não é estritamente necessário. No entanto, é bom ter se o Auth service usa.
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date; // IMPORTANTE: Adicione esta importação
import java.util.Base64;
import java.util.function.Function;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Embora não seja usado para *gerar* tokens neste serviço, é bom tê-lo se o Auth service o utiliza
    @Value("${jwt.expiration}")
    private long expiration; // Adicione esta linha, mesmo que não seja usada diretamente para validação de expiração, a classe original JwtUtil do Auth service a tem.

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Método para extrair todos os claims do token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // Método genérico para extrair um claim específico
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // MÉTODO NOVO: Extrai o nome de usuário (email) do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // MÉTODO NOVO: Extrai a data de expiração do token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // MÉTODO NOVO: Verifica se o token expirou
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Método para extrair o userId (já existia)
    public UUID extractUserId(String token) {
        String userIdString = extractClaim(token, claims -> claims.get("userId", String.class));
        if (userIdString == null) {
            throw new IllegalArgumentException("Claim 'userId' não encontrada no token.");
        }
        return UUID.fromString(userIdString);
    }

    // MÉTODO NOVO: Valida o token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}