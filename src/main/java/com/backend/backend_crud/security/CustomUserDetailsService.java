package com.backend.backend_crud.security;

import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user trong DB bằng email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Map Role của user thành Authority của Security
        String roleName = user.getRole().getRoleName();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        // 3. Trả về đối tượng UserDetails chuẩn của Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getIsActive(), // enable
                true, true, true,
                Collections.singletonList(authority) // Danh sách quyền
        );
    }
}