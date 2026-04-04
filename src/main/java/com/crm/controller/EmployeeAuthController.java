package com.crm.controller;

import com.crm.dto.*;
import com.crm.security.JwtUtils;
import com.crm.service.EmployeeAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/auth")
@RequiredArgsConstructor
@Slf4j
public class EmployeeAuthController {

    private final EmployeeAuthService employeeAuthService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> employeeLogin(
            @Valid @RequestBody EmployeeLoginRequest loginRequest,
            HttpServletRequest request) {
        log.info("Employee login attempt for email: {}", loginRequest.getEmail());

        LoginResponse response = employeeAuthService.authenticateEmployee(loginRequest);

        return ResponseEntity.ok(ApiResponse.success(response, "Login successful", request.getRequestURI()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest passwordRequest,
            HttpServletRequest request) {
        log.info("Password change request received");

        employeeAuthService.changePassword(passwordRequest);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", request.getRequestURI()));
    }
}