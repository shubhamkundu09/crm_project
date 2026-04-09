package com.crm.controller;

import com.crm.dto.ApiResponse;
import com.crm.dto.LeadResponseDTO;
import com.crm.dto.WebsiteLeadDTO;
import com.crm.service.WebsiteLeadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/website")
@RequiredArgsConstructor
@Slf4j
public class WebsiteController {

    private final WebsiteLeadService websiteLeadService;

    @PostMapping("/leads")
    public ResponseEntity<ApiResponse<LeadResponseDTO>> submitLead(
            @Valid @RequestBody WebsiteLeadDTO websiteLeadDTO,
            HttpServletRequest request) {
        log.info("New website lead submission from: {}", websiteLeadDTO.getEmail());
        LeadResponseDTO lead = websiteLeadService.submitWebsiteLead(websiteLeadDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lead,
                        "Lead submitted successfully! Our team will contact you within 24 hours.",
                        request.getRequestURI()));
    }
}