package com.crm.service;

import com.crm.dto.LeadResponseDTO;
import com.crm.dto.WebsiteLeadDTO;
import com.crm.dto.WebsiteLeadUpdateDTO;

import java.util.List;
import java.util.Map;

public interface WebsiteLeadService {
    LeadResponseDTO submitWebsiteLead(WebsiteLeadDTO websiteLeadDTO);
    List<LeadResponseDTO> getWebsiteLeads();
    LeadResponseDTO getWebsiteLeadById(Long id);
    LeadResponseDTO updateWebsiteLead(Long id, WebsiteLeadUpdateDTO updateDTO);
    void deleteWebsiteLead(Long id);
    Map<String, Long> getWebsiteLeadStatistics();
    List<LeadResponseDTO> getTodayWebsiteFollowUps();
    List<LeadResponseDTO> getPendingWebsiteFollowUps();
    LeadResponseDTO assignWebsiteLead(Long leadId, Long employeeId);
}