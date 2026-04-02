package com.crm.service;

import com.crm.dto.*;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import com.crm.exception.DuplicateResourceException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.repository.EmployeeRepository;
import com.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    @Override
    public LeadResponseDTO createLead(LeadDTO leadDTO) {
        log.info("Creating new lead with email: {}", leadDTO.getEmail());

        // Check for duplicate email
        if (leadRepository.existsByEmail(leadDTO.getEmail())) {
            throw new DuplicateResourceException("Lead with email already exists: " + leadDTO.getEmail());
        }

        // Get assigned employee
        Employee assignedEmployee = employeeRepository.findById(leadDTO.getAssignedEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + leadDTO.getAssignedEmployeeId()));

        Lead lead = mapToEntity(leadDTO, assignedEmployee);
        Lead savedLead = leadRepository.save(lead);

        // Send notification email to assigned employee
        emailService.sendLeadAssignmentEmail(
                assignedEmployee.getEmail(),
                assignedEmployee.getFirstName() + " " + assignedEmployee.getLastName(),
                lead.getName(),
                lead.getLeadType().getDescription()
        );

        log.info("Lead created successfully with ID: {}", savedLead.getId());
        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateLead(Long id, LeadUpdateDTO leadUpdateDTO) {
        log.info("Updating lead with ID: {}", id);

        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Update fields if present
        if (leadUpdateDTO.getName() != null) existingLead.setName(leadUpdateDTO.getName());
        if (leadUpdateDTO.getEmail() != null) {
            // Check email uniqueness
            if (!existingLead.getEmail().equals(leadUpdateDTO.getEmail()) &&
                    leadRepository.existsByEmail(leadUpdateDTO.getEmail())) {
                throw new DuplicateResourceException("Lead with email already exists: " + leadUpdateDTO.getEmail());
            }
            existingLead.setEmail(leadUpdateDTO.getEmail());
        }
        if (leadUpdateDTO.getPhoneNumber() != null) existingLead.setPhoneNumber(leadUpdateDTO.getPhoneNumber());
        if (leadUpdateDTO.getLeadType() != null) existingLead.setLeadType(leadUpdateDTO.getLeadType());
        if (leadUpdateDTO.getLeadStage() != null) existingLead.setLeadStage(leadUpdateDTO.getLeadStage());
        if (leadUpdateDTO.getNextFollowUpDate() != null) existingLead.setNextFollowUpDate(leadUpdateDTO.getNextFollowUpDate());
        if (leadUpdateDTO.getRemarks() != null) existingLead.setRemarks(leadUpdateDTO.getRemarks());
        if (leadUpdateDTO.getNextFollowUp() != null) existingLead.setNextFollowUp(leadUpdateDTO.getNextFollowUp());
        if (leadUpdateDTO.getSource() != null) existingLead.setSource(leadUpdateDTO.getSource());

        if (leadUpdateDTO.getAssignedEmployeeId() != null) {
            Employee newEmployee = employeeRepository.findById(leadUpdateDTO.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + leadUpdateDTO.getAssignedEmployeeId()));
            existingLead.setAssignedEmployee(newEmployee);
        }

        Lead updatedLead = leadRepository.save(existingLead);
        log.info("Lead updated successfully with ID: {}", id);

        return mapToResponseDTO(updatedLead);
    }

    @Override
    public void deleteLead(Long id) {
        log.info("Deleting lead with ID: {}", id);

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Soft delete
        lead.setIsActive(false);
        leadRepository.save(lead);

        log.info("Lead deleted successfully with ID: {}", id);
    }

    @Override
    public LeadResponseDTO getLeadById(Long id) {
        log.info("Fetching lead with ID: {}", id);

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        return mapToResponseDTO(lead);
    }

    @Override
    public List<LeadResponseDTO> getAllLeads() {
        log.info("Fetching all leads");

        return leadRepository.findAll()
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getLeadsByEmployee(Long employeeId) {
        log.info("Fetching leads for employee ID: {}", employeeId);

        return leadRepository.findByAssignedEmployeeId(employeeId)
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getLeadsByType(LeadType leadType) {
        log.info("Fetching leads by type: {}", leadType);

        return leadRepository.findByLeadType(leadType)
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getLeadsByStage(LeadStage leadStage) {
        log.info("Fetching leads by stage: {}", leadStage);

        return leadRepository.findByLeadStage(leadStage)
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getTodayFollowUps() {
        log.info("Fetching today's follow-ups");

        return leadRepository.findByNextFollowUpDate(LocalDate.now())
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getPendingFollowUps() {
        log.info("Fetching pending follow-ups");

        return leadRepository.findByNextFollowUpDateBefore(LocalDate.now())
                .stream()
                .filter(Lead::getIsActive)
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponseDTO updateLeadStatistics(Long id, LeadStatisticsUpdateDTO statisticsDTO) {
        log.info("Updating statistics for lead ID: {}", id);

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        if (statisticsDTO.getWhatsappSent() != null && statisticsDTO.getWhatsappSent()) {
            lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
        }
        if (statisticsDTO.getCallsMade() != null && statisticsDTO.getCallsMade()) {
            lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
        }
        if (statisticsDTO.getFollowUp() != null && statisticsDTO.getFollowUp()) {
            lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
        }
        if (statisticsDTO.getMeetingBooked() != null && statisticsDTO.getMeetingBooked()) {
            lead.setMeetingsBookedCount(lead.getMeetingsBookedCount() + 1);
        }
        if (statisticsDTO.getMeetingDone() != null && statisticsDTO.getMeetingDone()) {
            lead.setMeetingsDoneCount(lead.getMeetingsDoneCount() + 1);
        }

        Lead updatedLead = leadRepository.save(lead);
        return mapToResponseDTO(updatedLead);
    }

    @Override
    public Map<String, Long> getLeadStatistics() {
        log.info("Fetching lead statistics");

        Map<String, Long> statistics = new HashMap<>();

        // Count by type
        List<Object[]> typeStats = leadRepository.countLeadsByType();
        for (Object[] stat : typeStats) {
            statistics.put("type_" + stat[0].toString().toLowerCase(), (Long) stat[1]);
        }

        // Count by stage
        List<Object[]> stageStats = leadRepository.countLeadsByStage();
        for (Object[] stat : stageStats) {
            statistics.put("stage_" + stat[0].toString().toLowerCase(), (Long) stat[1]);
        }

        // Total active leads
        statistics.put("total_active_leads", leadRepository.findByIsActiveTrue().stream().count());

        return statistics;
    }

    @Override
    public List<LeadResponseDTO> getLeadsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching leads between {} and {}", startDate, endDate);

        return leadRepository.findAll()
                .stream()
                .filter(lead -> lead.getCreatedAt().toLocalDate().isAfter(startDate.minusDays(1)) &&
                        lead.getCreatedAt().toLocalDate().isBefore(endDate.plusDays(1)) &&
                        lead.getIsActive())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private Lead mapToEntity(LeadDTO dto, Employee assignedEmployee) {
        return Lead.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .leadType(dto.getLeadType())
                .leadStage(dto.getLeadStage())
                .nextFollowUpDate(dto.getNextFollowUpDate())
                .remarks(dto.getRemarks())
                .nextFollowUp(dto.getNextFollowUp())
                .assignedEmployee(assignedEmployee)
                .source(dto.getSource())
                .isActive(true)
                .build();
    }

    private LeadResponseDTO mapToResponseDTO(Lead lead) {
        return LeadResponseDTO.builder()
                .id(lead.getId())
                .name(lead.getName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .leadType(lead.getLeadType())
                .leadStage(lead.getLeadStage())
                .nextFollowUpDate(lead.getNextFollowUpDate())
                .remarks(lead.getRemarks())
                .nextFollowUp(lead.getNextFollowUp())
                .assignedEmployee(mapToEmployeeResponseDTO(lead.getAssignedEmployee()))
                .source(lead.getSource())
                .isActive(lead.getIsActive())
                .whatsappSentCount(lead.getWhatsappSentCount())
                .callsMadeCount(lead.getCallsMadeCount())
                .followUpsCount(lead.getFollowUpsCount())
                .meetingsBookedCount(lead.getMeetingsBookedCount())
                .meetingsDoneCount(lead.getMeetingsDoneCount())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    private EmployeeResponseDTO mapToEmployeeResponseDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .build();
    }
}