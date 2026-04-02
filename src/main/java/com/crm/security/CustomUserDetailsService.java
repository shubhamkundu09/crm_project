// CustomUserDetailsService.java (updated)
package com.crm.security;

import com.crm.entity.Employee;
import com.crm.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check if it's admin login
        if (adminUsername.equals(username)) {
            log.info("Admin user found: {}", username);
            return new AdminUserDetails(adminUsername, passwordEncoder.encode(adminPassword));
        }

        // Check if it's employee login (by email)
        Employee employee = employeeRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        log.info("Employee found: {}", employee.getEmail());
        return new EmployeeUserDetails(employee);
    }

    @PostConstruct
    public void init() {
        log.info("User details service initialized with admin username: {}", adminUsername);
    }
}