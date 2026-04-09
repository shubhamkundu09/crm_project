package com.crm.controller;

import com.crm.dto.ApiResponse;
import com.crm.dto.ChangePasswordRequest;
import com.crm.dto.PasswordResetDTO;
import com.crm.dto.UnifiedLoginRequest;
import com.crm.dto.UnifiedLoginResponse;
import com.crm.service.EmployeeService;
import com.crm.service.UnifiedAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UnifiedAuthService unifiedAuthService;
    private final EmployeeService employeeService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> login(
            @Valid @RequestBody UnifiedLoginRequest loginRequest,
            HttpServletRequest request) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        UnifiedLoginResponse response = unifiedAuthService.authenticate(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage(), request.getRequestURI()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        log.info("Logout request received");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("Logout successful", request.getRequestURI()));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(HttpServletRequest request) {
        log.info("Token validation request received");
        return ResponseEntity.ok(ApiResponse.success("Token is valid", "Token validation successful", request.getRequestURI()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest passwordRequest,
            HttpServletRequest request) {
        String email = authentication.getName();
        employeeService.changePassword(email, passwordRequest);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", request.getRequestURI()));
    }

    @PostMapping("/admin/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetEmployeePassword(
            @Valid @RequestBody PasswordResetDTO passwordResetDTO,
            HttpServletRequest request) {
        log.info("Admin resetting password for employee: {}", passwordResetDTO.getEmail());
        employeeService.resetEmployeePassword(passwordResetDTO);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. New password has been sent to employee's email.", request.getRequestURI()));
    }
}