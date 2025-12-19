package com.backend.backend_crud.service;

import com.backend.backend_crud.config.JwtProperties;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.exception.TokenException;
import com.backend.backend_crud.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    /**
     * Tạo access token cho user
     *
     * @param user User entity
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("scope", user.getScope().name());
        claims.put("roleId", user.getRole().getId());
        if (user.getSchool() != null) {
            claims.put("schoolId", user.getSchool().getId());
        }

        return jwtUtil.createToken(claims, user.getEmail(), jwtProperties.getExpiration());
    }


    /**
     * Validate access token (check format và expiration)
     * Email được extract từ chính token, không cần truyền từ bên ngoài
     *
     * @param token JWT token
     * @return true nếu hợp lệ
     * @throws TokenException nếu token không hợp lệ
     */
    public boolean validateAccessToken(String token) {
        if (!jwtUtil.validateTokenFormat(token)) {
            throw new TokenException("Token không hợp lệ hoặc đã hết hạn");
        }
        return true;
    }

    /**
     * Validate access token với email check (dùng cho các trường hợp đặc biệt)
     *
     * @param token JWT token
     * @param email Email của user để validate
     * @return true nếu hợp lệ
     * @throws TokenException nếu token không hợp lệ hoặc không khớp email
     */
    public boolean validateAccessToken(String token, String email) {
        if (!validateAccessToken(token)) {
            return false;
        }

        String tokenEmail = jwtUtil.extractSubject(token);
        if (!email.equals(tokenEmail)) {
            throw new TokenException("Token không khớp với email");
        }

        return true;
    }


    /**
     * Extract user ID từ token
     *
     * @param token JWT token
     * @return User ID
     * @throws TokenException nếu không thể extract
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            Long userId = jwtUtil.extractLongClaim(claims, "userId");
            if (userId == null) {
                throw new TokenException("Token không chứa userId");
            }
            return userId;
        } catch (Exception e) {
            throw new TokenException("Không thể extract userId từ token: " + e.getMessage());
        }
    }

    /**
     * Extract email từ token
     *
     * @param token JWT token
     * @return Email của user
     * @throws TokenException nếu không thể extract
     */
    public String getEmailFromToken(String token) {
        try {
            return jwtUtil.extractSubject(token);
        } catch (Exception e) {
            throw new TokenException("Không thể extract email từ token: " + e.getMessage());
        }
    }

    /**
     * Extract scope từ token
     *
     * @param token JWT token
     * @return Scope của user
     * @throws TokenException nếu không thể extract
     */
    public String getScopeFromToken(String token) {
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            return jwtUtil.extractStringClaim(claims, "scope");
        } catch (Exception e) {
            throw new TokenException("Không thể extract scope từ token: " + e.getMessage());
        }
    }

    /**
     * Extract role ID từ token
     *
     * @param token JWT token
     * @return Role ID
     * @throws TokenException nếu không thể extract
     */
    public Long getRoleIdFromToken(String token) {
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            return jwtUtil.extractLongClaim(claims, "roleId");
        } catch (Exception e) {
            throw new TokenException("Không thể extract roleId từ token: " + e.getMessage());
        }
    }

    /**
     * Extract school ID từ token
     *
     * @param token JWT token
     * @return School ID (có thể null)
     * @throws TokenException nếu không thể extract
     */
    public Long getSchoolIdFromToken(String token) {
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            return jwtUtil.extractLongClaim(claims, "schoolId");
        } catch (Exception e) {
            throw new TokenException("Không thể extract schoolId từ token: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra token đã hết hạn chưa
     *
     * @param token JWT token
     * @return true nếu đã hết hạn, false nếu chưa
     */
    public boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token);
    }
}
