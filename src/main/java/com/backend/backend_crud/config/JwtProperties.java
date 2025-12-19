package com.backend.backend_crud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    /**
     * Secret key để ký JWT tokens
     * Nên là một chuỗi dài, ngẫu nhiên và bảo mật
     */
    private String secret = "your-secret-key-change-this-in-production-to-a-long-random-secure-string-at-least-256-bits";

    private Long expiration = 86400000L;
}
