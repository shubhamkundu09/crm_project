package com.crm.service;

import com.crm.dto.EmployeeLeadUpdateDTO;
import com.crm.dto.LeadResponseDTO;

public interface EmployeeLeadService {
    LeadResponseDTO updateLead(Long leadId, EmployeeLeadUpdateDTO updateDTO, String employeeEmail);
}