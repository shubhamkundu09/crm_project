// EmployeeAuthServiceImpl.java
package com.crm.service;

import com.crm.dto.ChangePasswordRequest;
import com.crm.dto.EmployeeLoginRequest;
import com.crm.dto.LoginResponse;
import com.crm.entity.Employee;
import com.crm.exception.ResourceNotFoundException;
import com.crm.exception.UnauthorizedException;
import com.crm.repository.EmployeeRepository;
import com.crm.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeAuthServiceImpl implements EmployeeAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public LoginResponse authenticateEmployee(EmployeeLoginRequest loginRequest) {
        // Authenticate the employee
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get employee details
        Employee employee = employeeRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Check if it's first login
        boolean isFirstLogin = employee.getIsFirstLogin();

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        String message = isFirstLogin ?
                "Login successful. Please change your password." :
                "Login successful";

        return new LoginResponse(jwt, loginRequest.getEmail(), message);
    }

    @Override
    public void changePassword(ChangePasswordRequest passwordRequest) {
        // Get current authenticated user email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), employee.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update password
        employee.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        employee.setIsFirstLogin(false); // Mark first login as complete
        employeeRepository.save(employee);

        // Send confirmation email
        emailService.sendPasswordChangeConfirmation(employee.getEmail(),
                employee.getFirstName() + " " + employee.getLastName());

        log.info("Password changed successfully for employee: {}", email);
    }
}