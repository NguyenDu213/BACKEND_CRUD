package com.backend.backend_crud.config;

import com.backend.backend_crud.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Bỏ qua filter cho các endpoint public
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy token từ header
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract token
            final String jwt = authHeader.substring(7);

            // Validate token
            if (jwtService.validateAccessToken(jwt)) {
                // Lấy thông tin từ token
                String email = jwtService.getEmailFromToken(jwt);
                String scope = jwtService.getScopeFromToken(jwt);

                // Tạo authentication object
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + scope);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Collections.singletonList(authority));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Nếu có lỗi, không set authentication (sẽ bị reject bởi SecurityConfig)
            logger.error("JWT validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
