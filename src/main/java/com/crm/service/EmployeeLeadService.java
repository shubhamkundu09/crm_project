package com.crm.service;

import com.crm.dto.EmployeeLeadUpdateDTO;
import com.crm.dto.LeadResponseDTO;
import com.crm.dto.LeadStatisticsUpdateDTO;

public interface EmployeeLeadService {
    LeadResponseDTO updateLeadAfterContact(Long leadId, EmployeeLeadUpdateDTO updateDTO, String employeeEmail);
    LeadResponseDTO updateLeadStatistics(Long leadId, LeadStatisticsUpdateDTO statisticsDTO, String employeeEmail);
    LeadResponseDTO updateLeadStage(Long leadId, String stage, String employeeEmail);
    LeadResponseDTO updateFollowUp(Long leadId, String nextFollowUpDate, String nextFollowUpDescription, String employeeEmail);
}