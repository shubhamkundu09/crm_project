package com.crm.service;

import com.crm.dto.*;
import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LeadService {
    LeadResponseDTO createLead(LeadDTO leadDTO);
    LeadResponseDTO updateLead(Long id, LeadUpdateDTO leadUpdateDTO);
    void deleteLead(Long id);
    LeadResponseDTO getLeadById(Long id);
    List<LeadResponseDTO> getAllLeads();
    List<LeadResponseDTO> getLeadsByEmployee(Long employeeId);
    List<LeadResponseDTO> getLeadsByType(LeadType leadType);
    List<LeadResponseDTO> getLeadsByStage(LeadStage leadStage);
    List<LeadResponseDTO> getTodayFollowUps();
    List<LeadResponseDTO> getPendingFollowUps();
    LeadResponseDTO updateLeadStatistics(Long id, LeadStatisticsUpdateDTO statisticsDTO);
    Map<String, Long> getLeadStatistics();
    List<LeadResponseDTO> getLeadsByDateRange(LocalDate startDate, LocalDate endDate);
    LeadResponseDTO updateLeadStage(Long id, String stage, String employeeEmail);
    LeadResponseDTO updateFollowUp(Long id, String nextFollowUpDate, String nextFollowUpDescription, String employeeEmail);
    LeadResponseDTO updateLeadAfterContact(Long id, EmployeeLeadUpdateDTO updateDTO, String employeeEmail);
}