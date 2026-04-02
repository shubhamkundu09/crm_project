package com.crm.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminUsername.equals(username)) {
            log.info("Admin user found: {}", username);
            return new AdminUserDetails(adminUsername, passwordEncoder.encode(adminPassword));
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    @PostConstruct
    public void init() {
        log.info("Admin user configured with username: {}", adminUsername);
    }
}