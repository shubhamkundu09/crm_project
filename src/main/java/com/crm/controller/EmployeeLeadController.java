package com.crm.controller;

import com.crm.dto.ApiResponse;
import com.crm.dto.EmployeeLeadUpdateDTO;
import com.crm.dto.LeadResponseDTO;
import com.crm.dto.LeadStatisticsUpdateDTO;
import com.crm.service.EmployeeLeadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
@Slf4j
public class EmployeeLeadController {

    private final EmployeeLeadService employeeLeadService;

    @PatchMapping("/{leadId}/contact")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateLeadAfterContact(
            @PathVariable Long leadId,
            @Valid @RequestBody EmployeeLeadUpdateDTO updateDTO,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee updating lead after contact - Lead ID: {}", leadId);
        String email = authentication.getName();
        LeadResponseDTO updatedLead = employeeLeadService.updateLeadAfterContact(leadId, updateDTO, email);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead updated successfully", request.getRequestURI()));
    }

    @PatchMapping("/{leadId}/statistics")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateLeadStatistics(
            @PathVariable Long leadId,
            @Valid @RequestBody LeadStatisticsUpdateDTO statisticsDTO,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee updating statistics for lead ID: {}", leadId);
        String email = authentication.getName();
        LeadResponseDTO updatedLead = employeeLeadService.updateLeadStatistics(leadId, statisticsDTO, email);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead statistics updated successfully", request.getRequestURI()));
    }

    @PatchMapping("/{leadId}/stage")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateLeadStage(
            @PathVariable Long leadId,
            @RequestParam String stage,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee updating lead stage - Lead ID: {}, Stage: {}", leadId, stage);
        String email = authentication.getName();
        LeadResponseDTO updatedLead = employeeLeadService.updateLeadStage(leadId, stage, email);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Lead stage updated successfully", request.getRequestURI()));
    }

    @PostMapping("/{leadId}/followup")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateFollowUp(
            @PathVariable Long leadId,
            @RequestParam String nextFollowUpDate,
            @RequestParam String nextFollowUpDescription,
            Authentication authentication,
            HttpServletRequest request) {
        log.info("Employee updating follow-up for lead ID: {}", leadId);
        String email = authentication.getName();
        LeadResponseDTO updatedLead = employeeLeadService.updateFollowUp(leadId, nextFollowUpDate, nextFollowUpDescription, email);
        return ResponseEntity.ok(ApiResponse.success(updatedLead, "Follow-up updated successfully", request.getRequestURI()));
    }
}