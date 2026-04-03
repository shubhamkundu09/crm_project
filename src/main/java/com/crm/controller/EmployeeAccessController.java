package com.crm.controller;

import com.crm.dto.ApiResponse;
import com.crm.dto.EmployeeProfileDTO;
import com.crm.dto.LeadHistoryDTO;
import com.crm.dto.LeadResponseDTO;
import com.crm.service.EmployeeProfileService;
import com.crm.service.LeadHistoryService;
import com.crm.service.LeadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
@Slf4j
public class EmployeeAccessController {

    private final EmployeeProfileService employeeProfileService;
    private final LeadService leadService;
    private final LeadHistoryService leadHistoryService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployeeProfileDTO>> getMyProfile(Authentication authentication, HttpServletRequest request) {
        log.info("Employee fetching own profile");
        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully", request.getRequestURI()));
    }

    @GetMapping("/my-leads")
    public ResponseEntity<ApiResponse<List<LeadResponseDTO>>> getMyLeads(Authentication authentication, HttpServletRequest request) {
        log.info("Employee fetching assigned leads");
        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);
        List<LeadResponseDTO> leads = leadService.getLeadsByEmployee(profile.getId());
        return ResponseEntity.ok(ApiResponse.success(leads, "Your leads retrieved successfully", request.getRequestURI()));
    }

    @GetMapping("/my-leads/{leadId}")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> getMyLeadById(
            @PathVariable Long leadId,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee fetching lead details for ID: {}", leadId);
        LeadResponseDTO lead = leadService.getLeadById(leadId);

        // Verify employee has access to this lead
        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);
        if (!lead.getAssignedEmployee().getId().equals(profile.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have access to this lead", 403, request.getRequestURI()));
        }

        return ResponseEntity.ok(ApiResponse.success(lead, "Lead details retrieved successfully", request.getRequestURI()));
    }


    @GetMapping("/my-leads/{leadId}/history")
    public ResponseEntity<ApiResponse<List<LeadHistoryDTO>>> getMyLeadHistory(
            @PathVariable Long leadId,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee fetching lead history for ID: {}", leadId);
        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);

        LeadResponseDTO lead = leadService.getLeadById(leadId);
        if (!lead.getAssignedEmployee().getId().equals(profile.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have access to this lead", 403, request.getRequestURI()));
        }

        List<LeadHistoryDTO> history = leadHistoryService.getLeadHistory(leadId);
        return ResponseEntity.ok(ApiResponse.success(history, "Lead history retrieved successfully", request.getRequestURI()));
    }
}