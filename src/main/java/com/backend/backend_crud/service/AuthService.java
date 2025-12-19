package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.response.JwtResponse;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.exception.AuthenticationException;
import com.backend.backend_crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Xác thực user và tạo JWT token
     *
     * @param email    Email của user
     * @param password Password của user
     * @return JwtResponse chứa access token
     * @throws AuthenticationException nếu email hoặc password không đúng
     */
    public JwtResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Email hoặc mật khẩu không đúng"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Tài khoản đã bị vô hiệu hóa");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Email hoặc mật khẩu không đúng");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return buildJwtResponse(user, accessToken);
    }

    /**
     * Helper method để build JwtResponse từ User và token
     *
     * @param user User entity
     * @param accessToken Access token
     * @return JwtResponse
     */
    private JwtResponse buildJwtResponse(User user, String accessToken) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .scope(user.getScope().name())
                .roleId(user.getRole().getId())
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .build();
    }
}
