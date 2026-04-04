// WebsiteLeadController.java
package com.crm.controller;

import com.crm.dto.*;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import com.crm.repository.EmployeeRepository;
import com.crm.repository.LeadRepository;
import com.crm.service.EmailService;
import com.crm.service.LeadHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WebsiteLeadController {

    private final LeadRepository leadRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final LeadHistoryService leadHistoryService;

    // Public endpoint for website lead submission (no authentication required)
    // WebsiteLeadController.java - Updated submitWebsiteLead method

    @PostMapping("/api/website/leads")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> submitWebsiteLead(
            @Valid @RequestBody WebsiteLeadDTO websiteLeadDTO,
            HttpServletRequest request) {

        log.info("New website lead submission from: {}", websiteLeadDTO.getEmail());

        // Check for duplicate email
        if (leadRepository.existsByEmail(websiteLeadDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("A lead with this email already exists. Our team will contact you shortly.",
                            409, request.getRequestURI()));
        }

//        i am assigning website leads to admin then he can assign it to any other if he wants


        Employee defaultEmployee = employeeRepository.findByEmail("redcircle0908@gmail.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found. Please ensure admin exists."));


        // Create lead from website submission
        Lead lead = Lead.builder()
                .name(websiteLeadDTO.getName())
                .email(websiteLeadDTO.getEmail())
                .phoneNumber(websiteLeadDTO.getPhoneNumber())
                .leadType(LeadType.WARM) // Default to WARM for website leads
                .leadStage(LeadStage.LEAD_GENERATED)
                .nextFollowUpDate(LocalDate.now().plusDays(2))
                .remarks(websiteLeadDTO.getRemarks())
                .nextFollowUp("Initial contact - Customer inquiry from website")
                .source("Website Lead Form")
                .isActive(true)
                .interestedService(websiteLeadDTO.getInterestedService())
                .serviceSubcategory(websiteLeadDTO.getServiceSubcategory())
                .serviceSubSubcategory(websiteLeadDTO.getServiceSubSubcategory())
                .serviceDescription(websiteLeadDTO.getServiceDescription())
                .whatsappSentCount(0)
                .callsMadeCount(0)
                .followUpsCount(0)
                .meetingsBookedCount(0)
                .meetingsDoneCount(0)
                .updateCount(0)
                .lastUpdatedBy("WEBSITE")
                .assignedEmployee(defaultEmployee) // IMPORTANT: Assign an employee
                .build();

        Lead savedLead = leadRepository.save(lead);

        // Send notification email to admin
        emailService.sendWebsiteLeadNotification(websiteLeadDTO);

        log.info("Website lead saved successfully with ID: {}", savedLead.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapToResponseDTO(savedLead),
                        "Lead submitted successfully! Our team will contact you within 24 hours.",
                        request.getRequestURI()));
    }

    // Admin endpoint to get all website leads
    @GetMapping("/api/admin/website-leads")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LeadResponseDTO>>> getWebsiteLeads(HttpServletRequest request) {
        log.info("Fetching all website leads");

        List<LeadResponseDTO> websiteLeads = leadRepository.findAll()
                .stream()
                .filter(lead -> "Website Lead Form".equals(lead.getSource()) && lead.getIsActive())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(websiteLeads,
                "Website leads retrieved successfully", request.getRequestURI()));
    }

    // Admin endpoint to update website lead (assign employee, change type/stage)
    @PutMapping("/api/admin/website-leads/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> updateWebsiteLead(
            @PathVariable Long id,
            @RequestBody WebsiteLeadUpdateDTO updateDTO,
            HttpServletRequest request) {

        log.info("Admin updating website lead with ID: {}", id);

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (updateDTO.getLeadType() != null) {
            lead.setLeadType(updateDTO.getLeadType());
        }

        if (updateDTO.getLeadStage() != null) {
            lead.setLeadStage(updateDTO.getLeadStage());
        }

        if (updateDTO.getAssignedEmployeeId() != null) {
            employeeRepository.findById(updateDTO.getAssignedEmployeeId())
                    .ifPresent(lead::setAssignedEmployee);
        }

        if (updateDTO.getRemarks() != null) {
            lead.setRemarks(updateDTO.getRemarks());
        }

        if (updateDTO.getNextFollowUpDate() != null) {
            lead.setNextFollowUpDate(updateDTO.getNextFollowUpDate());
        }

        if (updateDTO.getNextFollowUp() != null) {
            lead.setNextFollowUp(updateDTO.getNextFollowUp());
        }

        lead.setLastUpdatedBy("ADMIN");
        lead.setUpdateCount(lead.getUpdateCount() + 1);

        Lead savedLead = leadRepository.save(lead);

        return ResponseEntity.ok(ApiResponse.success(mapToResponseDTO(savedLead),
                "Website lead updated successfully", request.getRequestURI()));
    }

    private LeadResponseDTO mapToResponseDTO(Lead lead) {
        LeadResponseDTO dto = new LeadResponseDTO();
        dto.setId(lead.getId());
        dto.setName(lead.getName());
        dto.setEmail(lead.getEmail());
        dto.setPhoneNumber(lead.getPhoneNumber());
        dto.setLeadType(lead.getLeadType());
        dto.setLeadStage(lead.getLeadStage());
        dto.setNextFollowUpDate(lead.getNextFollowUpDate());
        dto.setRemarks(lead.getRemarks());
        dto.setNextFollowUp(lead.getNextFollowUp());
        dto.setSource(lead.getSource());
        dto.setIsActive(lead.getIsActive());
        dto.setWhatsappSentCount(lead.getWhatsappSentCount());
        dto.setCallsMadeCount(lead.getCallsMadeCount());
        dto.setFollowUpsCount(lead.getFollowUpsCount());
        dto.setMeetingsBookedCount(lead.getMeetingsBookedCount());
        dto.setMeetingsDoneCount(lead.getMeetingsDoneCount());
        dto.setInterestedService(lead.getInterestedService());
        dto.setServiceSubcategory(lead.getServiceSubcategory());
        dto.setServiceSubSubcategory(lead.getServiceSubSubcategory());
        dto.setServiceDescription(lead.getServiceDescription());
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());
        dto.setUpdateCount(lead.getUpdateCount());
        dto.setLastUpdatedBy(lead.getLastUpdatedBy());

        if (lead.getAssignedEmployee() != null) {
            com.crm.dto.EmployeeResponseDTO empDto = new com.crm.dto.EmployeeResponseDTO();
            empDto.setId(lead.getAssignedEmployee().getId());
            empDto.setFirstName(lead.getAssignedEmployee().getFirstName());
            empDto.setLastName(lead.getAssignedEmployee().getLastName());
            empDto.setEmail(lead.getAssignedEmployee().getEmail());
            empDto.setEmployeeCode(lead.getAssignedEmployee().getEmployeeCode());
            dto.setAssignedEmployee(empDto);
        }

        return dto;
    }
}