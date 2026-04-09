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
import com.crm.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebsiteLeadServiceImpl implements WebsiteLeadService {

    private final LeadRepository leadRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final LeadHistoryService leadHistoryService;

    private static final String ADMIN_EMAIL = "redcircle0908@gmail.com";
    private static final String WEBSITE_SOURCE = "Website Lead Form";

    @Override
    public LeadResponseDTO submitWebsiteLead(WebsiteLeadDTO websiteLeadDTO) {
        log.info("Processing website lead submission from: {}", websiteLeadDTO.getEmail());

        if (leadRepository.existsByEmail(websiteLeadDTO.getEmail())) {
            throw new DuplicateResourceException("A lead with this email already exists. Our team will contact you shortly.");
        }

        Employee defaultEmployee = employeeRepository.findByEmail(ADMIN_EMAIL)
                .orElseThrow(() -> new RuntimeException("Admin user not found. Please ensure admin exists."));

        Lead lead = buildLeadFromWebsiteDTO(websiteLeadDTO, defaultEmployee);
        Lead savedLead = leadRepository.save(lead);

        emailService.sendWebsiteLeadNotification(websiteLeadDTO);

        log.info("Website lead saved successfully with ID: {}", savedLead.getId());
        return mapToResponseDTO(savedLead);
    }

    @Override
    public List<LeadResponseDTO> getWebsiteLeads() {
        log.info("Fetching all website leads");
        return leadRepository.findAll()
                .stream()
                .filter(lead -> WEBSITE_SOURCE.equals(lead.getSource()) && lead.getIsActive())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponseDTO getWebsiteLeadById(Long id) {
        log.info("Fetching website lead with ID: {}", id);
        Lead lead = leadRepository.findById(id)
                .filter(l -> WEBSITE_SOURCE.equals(l.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("Website lead not found with ID: " + id));
        return mapToResponseDTO(lead);
    }

    @Override
    public LeadResponseDTO updateWebsiteLead(Long id, WebsiteLeadUpdateDTO updateDTO) {
        log.info("Updating website lead with ID: {}", id);

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        Employee admin = employeeRepository.findByEmail(ADMIN_EMAIL)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<String> changes = new ArrayList<>();

        if (updateDTO.getLeadType() != null && updateDTO.getLeadType() != lead.getLeadType()) {
            changes.add("Lead Type changed from " + lead.getLeadType() + " to " + updateDTO.getLeadType());
            lead.setLeadType(updateDTO.getLeadType());
        }

        if (updateDTO.getLeadStage() != null && updateDTO.getLeadStage() != lead.getLeadStage()) {
            String oldStage = lead.getLeadStage().toString();
            String newStage = updateDTO.getLeadStage().toString();
            changes.add("Lead Stage changed from " + oldStage + " to " + newStage);
            leadHistoryService.recordStageChange(lead, admin, oldStage, newStage, "Stage updated by admin for website lead");
            lead.setLeadStage(updateDTO.getLeadStage());
        }

        if (updateDTO.getAssignedEmployeeId() != null) {
            Employee newEmployee = employeeRepository.findById(updateDTO.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + updateDTO.getAssignedEmployeeId()));

            if (!newEmployee.getId().equals(lead.getAssignedEmployee().getId())) {
                changes.add("Assigned employee changed from " + lead.getAssignedEmployee().getEmail() +
                        " to " + newEmployee.getEmail());
                lead.setAssignedEmployee(newEmployee);

                emailService.sendLeadAssignmentEmail(
                        newEmployee.getEmail(),
                        newEmployee.getFirstName() + " " + newEmployee.getLastName(),
                        lead.getName(),
                        lead.getLeadType().getDescription()
                );
            }
        }

        if (updateDTO.getRemarks() != null && !updateDTO.getRemarks().equals(lead.getRemarks())) {
            changes.add("Remarks updated");
            lead.setRemarks(updateDTO.getRemarks());
        }

        if (updateDTO.getNextFollowUpDate() != null && !updateDTO.getNextFollowUpDate().equals(lead.getNextFollowUpDate())) {
            changes.add("Follow-up date changed from " + lead.getNextFollowUpDate() +
                    " to " + updateDTO.getNextFollowUpDate());
            lead.setNextFollowUpDate(updateDTO.getNextFollowUpDate());
        }

        if (updateDTO.getNextFollowUp() != null && !updateDTO.getNextFollowUp().equals(lead.getNextFollowUp())) {
            changes.add("Follow-up description updated");
            lead.setNextFollowUp(updateDTO.getNextFollowUp());
        }

        lead.setLastUpdatedBy("ADMIN");
        lead.setUpdateCount(lead.getUpdateCount() + 1);

        Lead savedLead = leadRepository.save(lead);

        if (!changes.isEmpty()) {
            String changesText = String.join("; ", changes);
            leadHistoryService.recordLeadUpdate(savedLead, admin, changesText, "Website lead updated by admin");
        }

        log.info("Website lead {} updated successfully with {} changes", id, changes.size());
        return mapToResponseDTO(savedLead);
    }

    @Override
    public void deleteWebsiteLead(Long id) {
        log.info("Soft deleting website lead with ID: {}", id);
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));
        lead.setIsActive(false);
        lead.setLastUpdatedBy("ADMIN");
        leadRepository.save(lead);
        log.info("Website lead {} soft deleted successfully", id);
    }

    @Override
    public Map<String, Long> getWebsiteLeadStatistics() {
        log.info("Fetching website lead statistics");
        List<Lead> websiteLeads = leadRepository.findAll().stream()
                .filter(lead -> WEBSITE_SOURCE.equals(lead.getSource()))
                .collect(Collectors.toList());

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", (long) websiteLeads.size());
        statistics.put("active", websiteLeads.stream().filter(Lead::getIsActive).count());
        statistics.put("inactive", websiteLeads.stream().filter(l -> !l.getIsActive()).count());
        statistics.put("interested", websiteLeads.stream()
                .filter(l -> l.getLeadStage() == LeadStage.INTERESTED && l.getIsActive()).count());
        statistics.put("not_interested", websiteLeads.stream()
                .filter(l -> l.getLeadStage() == LeadStage.NOT_INTERESTED && l.getIsActive()).count());
        statistics.put("normal", websiteLeads.stream()
                .filter(l -> l.getLeadStage() == LeadStage.NORMAL && l.getIsActive()).count());
        statistics.put("hot_leads", websiteLeads.stream()
                .filter(l -> l.getLeadType() == LeadType.HOT && l.getIsActive()).count());
        statistics.put("warm_leads", websiteLeads.stream()
                .filter(l -> l.getLeadType() == LeadType.WARM && l.getIsActive()).count());
        statistics.put("cold_leads", websiteLeads.stream()
                .filter(l -> l.getLeadType() == LeadType.COLD && l.getIsActive()).count());

        return statistics;
    }

    @Override
    public List<LeadResponseDTO> getTodayWebsiteFollowUps() {
        log.info("Fetching today's follow-ups for website leads");
        return leadRepository.findByNextFollowUpDate(LocalDate.now())
                .stream()
                .filter(lead -> WEBSITE_SOURCE.equals(lead.getSource()) && lead.getIsActive())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeadResponseDTO> getPendingWebsiteFollowUps() {
        log.info("Fetching pending follow-ups for website leads");
        return leadRepository.findByNextFollowUpDateBefore(LocalDate.now())
                .stream()
                .filter(lead -> WEBSITE_SOURCE.equals(lead.getSource()) && lead.getIsActive())
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeadResponseDTO assignWebsiteLead(Long leadId, Long employeeId) {
        log.info("Assigning website lead {} to employee {}", leadId, employeeId);

        Lead lead = leadRepository.findById(leadId)
                .filter(l -> WEBSITE_SOURCE.equals(l.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("Website lead not found with ID: " + leadId));

        Employee newEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String oldEmployeeEmail = lead.getAssignedEmployee().getEmail();
        lead.setAssignedEmployee(newEmployee);
        lead.setLastUpdatedBy("ADMIN");

        Lead savedLead = leadRepository.save(lead);

        Employee admin = employeeRepository.findByEmail(ADMIN_EMAIL)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        String changes = "Assigned employee changed from " + oldEmployeeEmail + " to " + newEmployee.getEmail();
        leadHistoryService.recordLeadUpdate(savedLead, admin, changes, "Website lead reassigned by admin");

        emailService.sendLeadAssignmentEmail(
                newEmployee.getEmail(),
                newEmployee.getFirstName() + " " + newEmployee.getLastName(),
                lead.getName(),
                lead.getLeadType().getDescription()
        );

        log.info("Website lead {} assigned to {} successfully", leadId, newEmployee.getEmail());
        return mapToResponseDTO(savedLead);
    }

    private Lead buildLeadFromWebsiteDTO(WebsiteLeadDTO dto, Employee defaultEmployee) {
        return Lead.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .leadType(LeadType.WARM)
                .leadStage(LeadStage.NORMAL)
                .nextFollowUpDate(LocalDate.now().plusDays(2))
                .remarks(dto.getRemarks() != null ? dto.getRemarks() : "New website lead")
                .nextFollowUp("Initial contact - Customer inquiry from website")
                .source(WEBSITE_SOURCE)
                .isActive(true)
                .interestedService(dto.getInterestedService())
                .serviceSubcategory(dto.getServiceSubcategory())
                .serviceSubSubcategory(dto.getServiceSubSubcategory())
                .serviceDescription(dto.getServiceDescription())
                .callsMadeCount(0)
                .meetingsBookedCount(0)
                .meetingsDoneCount(0)
                .updateCount(0)
                .lastUpdatedBy("WEBSITE")
                .assignedEmployee(defaultEmployee)
                .build();
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
        dto.setCallsMadeCount(lead.getCallsMadeCount());
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
            EmployeeResponseDTO empDto = new EmployeeResponseDTO();
            empDto.setId(lead.getAssignedEmployee().getId());
            empDto.setFirstName(lead.getAssignedEmployee().getFirstName());
            empDto.setLastName(lead.getAssignedEmployee().getLastName());
            empDto.setEmail(lead.getAssignedEmployee().getEmail());
            empDto.setEmployeeCode(lead.getAssignedEmployee().getEmployeeCode());
            empDto.setDepartment(lead.getAssignedEmployee().getDepartment());
            empDto.setPosition(lead.getAssignedEmployee().getPosition());
            dto.setAssignedEmployee(empDto);
        }

        return dto;
    }
}