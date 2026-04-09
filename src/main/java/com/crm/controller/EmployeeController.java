package com.crm.controller;

import com.crm.dto.*;
import com.crm.service.EmployeeLeadService;
import com.crm.service.EmployeeProfileService;
import com.crm.service.LeadHistoryService;
import com.crm.service.LeadService;
import com.crm.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
public class EmployeeController {

    private final EmployeeProfileService employeeProfileService;
    private final LeadService leadService;
    private final EmployeeLeadService employeeLeadService;
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
            @PathVariable String leadId,
            Authentication authentication,
            HttpServletRequest request) {
        Long decryptedLeadId = CryptoUtil.decryptToLong(leadId);
        log.info("Employee fetching lead details for ID: {}", decryptedLeadId);
        LeadResponseDTO lead = leadService.getLeadById(decryptedLeadId);

        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);
        if (!lead.getAssignedEmployee().getId().equals(profile.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have access to this lead", 403, request.getRequestURI()));
        }

        return ResponseEntity.ok(ApiResponse.success(lead, "Lead details retrieved successfully", request.getRequestURI()));
    }

    @PutMapping("/my-leads/{leadId}/update")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateMyLead(
            @PathVariable String leadId,
            @Valid @RequestBody EmployeeLeadUpdateDTO updateDTO,
            Authentication authentication,
            HttpServletRequest request) {
        Long decryptedLeadId = CryptoUtil.decryptToLong(leadId);
        log.info("Employee updating lead - Lead ID: {}", decryptedLeadId);
        String email = authentication.getName();
        LeadResponseDTO updatedLead = employeeLeadService.updateLead(decryptedLeadId, updateDTO, email);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead updated successfully", request.getRequestURI()));
    }

    @GetMapping("/my-leads/{leadId}/history")
    public ResponseEntity<ApiResponse<List<LeadHistoryDTO>>> getMyLeadHistory(
            @PathVariable String leadId,
            Authentication authentication,
            HttpServletRequest request) {
        Long decryptedLeadId = CryptoUtil.decryptToLong(leadId);
        log.info("Employee fetching lead history for ID: {}", decryptedLeadId);
        String email = authentication.getName();
        EmployeeProfileDTO profile = employeeProfileService.getEmployeeProfileByEmail(email);

        LeadResponseDTO lead = leadService.getLeadById(decryptedLeadId);
        if (!lead.getAssignedEmployee().getId().equals(profile.getId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You don't have access to this lead", 403, request.getRequestURI()));
        }

        List<LeadHistoryDTO> history = leadHistoryService.getLeadHistory(decryptedLeadId);
        return ResponseEntity.ok(ApiResponse.success(history, "Lead history retrieved successfully", request.getRequestURI()));
    }
}