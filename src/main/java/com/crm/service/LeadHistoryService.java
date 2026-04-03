package com.crm.service;

import com.crm.dto.LeadHistoryDTO;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.entity.LeadHistory;
import com.crm.repository.LeadHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadHistoryService {

    private final LeadHistoryRepository leadHistoryRepository;

    public void recordLeadCreation(Lead lead, Employee employee) {
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("CREATE")
                .changes("Lead created with type: " + lead.getLeadType() + ", stage: " + lead.getLeadStage())
                .remarks("Initial lead creation by " + employee.getEmail())
                .previousStage("NONE")
                .newStage(lead.getLeadStage().toString())
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded lead creation history for lead ID: {}", lead.getId());
    }

    public void recordLeadUpdate(Lead lead, Employee employee, String changes, String remarks) {
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("UPDATE")
                .changes(changes)
                .remarks(remarks)
                .previousStage(lead.getLeadStage().toString())
                .newStage(lead.getLeadStage().toString())
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded lead update history for lead ID: {}", lead.getId());
    }

    public void recordStageChange(Lead lead, Employee employee, String previousStage, String newStage, String remarks) {
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("STAGE_CHANGE")
                .changes("Stage changed from " + previousStage + " to " + newStage)
                .remarks(remarks)
                .previousStage(previousStage)
                .newStage(newStage)
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded stage change history for lead ID: {} from {} to {}", lead.getId(), previousStage, newStage);
    }

    public void recordStatisticsUpdate(Lead lead, Employee employee, String statisticsChanges, String remarks) {
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("STATISTICS_UPDATE")
                .changes(statisticsChanges)
                .remarks(remarks)
                .previousStage(lead.getLeadStage().toString())
                .newStage(lead.getLeadStage().toString())
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded statistics update history for lead ID: {}", lead.getId());
    }

    public void recordFollowUpUpdate(Lead lead, Employee employee, String oldFollowUpDate, String newFollowUpDate, String oldDescription, String newDescription) {
        String changes = String.format("Follow-up date changed from %s to %s. Description: %s",
                oldFollowUpDate, newFollowUpDate, newDescription);
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("FOLLOWUP_UPDATE")
                .changes(changes)
                .remarks(newDescription)
                .previousStage(lead.getLeadStage().toString())
                .newStage(lead.getLeadStage().toString())
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded follow-up update history for lead ID: {}", lead.getId());
    }

    public void recordContactMade(Lead lead, Employee employee, String contactMethod, String response, String remarks) {
        String changes = String.format("Contact made via %s. Response: %s", contactMethod, response);
        LeadHistory history = LeadHistory.builder()
                .lead(lead)
                .employee(employee)
                .action("CONTACT_MADE")
                .changes(changes)
                .remarks(remarks)
                .previousStage(lead.getLeadStage().toString())
                .newStage(lead.getLeadStage().toString())
                .build();
        leadHistoryRepository.save(history);
        log.info("Recorded contact made history for lead ID: {}", lead.getId());
    }

    public List<LeadHistoryDTO> getLeadHistory(Long leadId) {
        return leadHistoryRepository.findByLeadIdOrderByCreatedAtDesc(leadId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<LeadHistoryDTO> getEmployeeLeadHistory(Long employeeId) {
        return leadHistoryRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private LeadHistoryDTO mapToDTO(LeadHistory history) {
        LeadHistoryDTO dto = new LeadHistoryDTO();
        dto.setId(history.getId());
        dto.setAction(history.getAction());
        dto.setChanges(history.getChanges());
        dto.setRemarks(history.getRemarks());
        dto.setPreviousStage(history.getPreviousStage());
        dto.setNewStage(history.getNewStage());
        dto.setCreatedAt(history.getCreatedAt());

        if (history.getEmployee() != null) {
            com.crm.dto.EmployeeBasicDTO employeeDTO = new com.crm.dto.EmployeeBasicDTO();
            employeeDTO.setId(history.getEmployee().getId());
            employeeDTO.setFirstName(history.getEmployee().getFirstName());
            employeeDTO.setLastName(history.getEmployee().getLastName());
            employeeDTO.setEmail(history.getEmployee().getEmail());
            dto.setEmployee(employeeDTO);
        }

        return dto;
    }
}