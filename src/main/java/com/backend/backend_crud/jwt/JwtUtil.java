package com.backend.backend_crud.jwt;

import com.backend.backend_crud.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    /**
     * Khởi tạo SecretKey một lần duy nhất khi Bean được tạo
     * Tối ưu hiệu năng: tránh tạo Key mới mỗi lần tạo/validate token
     */
    @PostConstruct
    private void initSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    /**
     * Tạo JWT token với claims
     * @param claims Map chứa các claims
     * @param subject Subject của token (thường là email)
     * @param expiration Thời gian hết hạn (milliseconds)
     * @return JWT token string
     */
    public String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract tất cả claims từ token
     *
     * @param token JWT token
     * @return Claims object
     * @throws io.jsonwebtoken.JwtException nếu token không hợp lệ
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract một claim cụ thể từ token
     * @param token JWT token
     * @param claimsResolver Function để extract claim
     * @return Giá trị của claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract subject (email) từ token
     * @param token JWT token
     * @return Subject (email)
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract ngày hết hạn từ token
     * @param token JWT token
     * @return Ngày hết hạn
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     * @param token JWT token
     * @return true nếu đã hết hạn, false nếu chưa
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token format và expiration (không check business logic)
     *
     * @param token JWT token
     * @return true nếu token có format hợp lệ và chưa hết hạn, false nếu không
     */
    public Boolean validateTokenFormat(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Helper method để extract Long value từ claims một cách an toàn
     * 
     * @param claims Claims object
     * @param key Key của claim
     * @return Long value hoặc null nếu không tồn tại
     */
    public Long extractLongClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        throw new IllegalArgumentException("Claim '" + key + "' không phải là số");
    }

    /**
     * Helper method để extract String value từ claims một cách an toàn
     * 
     * @param claims Claims object
     * @param key Key của claim
     * @return String value hoặc null nếu không tồn tại
     */
    public String extractStringClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
