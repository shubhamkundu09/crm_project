package com.crm.service;

import com.crm.dto.EmployeeLeadUpdateDTO;
import com.crm.dto.EmployeeResponseDTO;
import com.crm.dto.LeadResponseDTO;
import com.crm.dto.LeadStatisticsUpdateDTO;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.entity.LeadStage;
import com.crm.exception.ResourceNotFoundException;
import com.crm.exception.UnauthorizedException;
import com.crm.repository.EmployeeRepository;
import com.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeLeadServiceImpl implements EmployeeLeadService {

    private final LeadRepository leadRepository;
    private final EmployeeRepository employeeRepository;
    private final LeadHistoryService leadHistoryService;

    @Override
    public LeadResponseDTO updateLeadAfterContact(Long leadId, EmployeeLeadUpdateDTO updateDTO, String employeeEmail) {
        log.info("Employee {} updating lead {} after contact", employeeEmail, leadId);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        List<String> changes = new ArrayList<>();
        String oldStage = lead.getLeadStage().toString();
        String newStage = oldStage;
        String contactInfo = "";

        // Track if any contact was made
        if (Boolean.TRUE.equals(updateDTO.getContactMade())) {
            contactInfo = "Contact made. ";

            if (updateDTO.getResponseMessage() != null) {
                if (updateDTO.getResponseMessage().toLowerCase().contains("call")) {
                    lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                    contactInfo += "Phone call recorded. ";
                } else if (updateDTO.getResponseMessage().toLowerCase().contains("whatsapp")) {
                    lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                    contactInfo += "WhatsApp message sent. ";
                }
                lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
                contactInfo += "Follow-up recorded. ";
            }
            lead.setLastContactDate(LocalDateTime.now());
            changes.add(contactInfo);
        }

        // Update lead stage if provided
        if (updateDTO.getNewLeadStage() != null && updateDTO.getNewLeadStage() != lead.getLeadStage()) {
            newStage = updateDTO.getNewLeadStage().toString();
            lead.setLeadStage(updateDTO.getNewLeadStage());
            changes.add("Stage changed from " + oldStage + " to " + newStage);
        }

        // Update remarks
        if (updateDTO.getRemarks() != null && !updateDTO.getRemarks().isEmpty()) {
            lead.setRemarks(updateDTO.getRemarks());
            changes.add("Remarks updated: " + updateDTO.getRemarks());
        }

        // Handle conversion to customer
        if (Boolean.TRUE.equals(updateDTO.getConvertToCustomer())) {
            newStage = LeadStage.CLOSED.toString();
            lead.setLeadStage(LeadStage.CLOSED);
            changes.add("Lead converted to customer!");
        }

        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        Lead savedLead = leadRepository.save(lead);

        // Create SINGLE history entry for all changes
        if (!changes.isEmpty()) {
            String allChanges = String.join(". ", changes);
            leadHistoryService.recordLeadUpdate(savedLead, employee, allChanges, "Updated by employee: " + employee.getEmail());
        }

        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateLeadStatistics(Long leadId, LeadStatisticsUpdateDTO statisticsDTO, String employeeEmail) {
        log.info("Employee {} updating statistics for lead {}", employeeEmail, leadId);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        List<String> changes = new ArrayList<>();
        String oldStage = lead.getLeadStage().toString();
        String newStage = oldStage;

        if (statisticsDTO.getWhatsappSent() != null && statisticsDTO.getWhatsappSent()) {
            lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
            changes.add("WhatsApp sent (Total: " + lead.getWhatsappSentCount() + ")");
        }
        if (statisticsDTO.getCallsMade() != null && statisticsDTO.getCallsMade()) {
            lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
            changes.add("Phone call made (Total: " + lead.getCallsMadeCount() + ")");
        }
        if (statisticsDTO.getFollowUp() != null && statisticsDTO.getFollowUp()) {
            lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
            changes.add("Follow-up completed (Total: " + lead.getFollowUpsCount() + ")");
        }
        if (statisticsDTO.getMeetingBooked() != null && statisticsDTO.getMeetingBooked()) {
            lead.setMeetingsBookedCount(lead.getMeetingsBookedCount() + 1);
            changes.add("Meeting booked (Total: " + lead.getMeetingsBookedCount() + ")");
            if (lead.getLeadStage() != LeadStage.MEETING_BOOKED) {
                newStage = LeadStage.MEETING_BOOKED.toString();
                lead.setLeadStage(LeadStage.MEETING_BOOKED);
                changes.add("Stage changed from " + oldStage + " to " + newStage);
            }
        }
        if (statisticsDTO.getMeetingDone() != null && statisticsDTO.getMeetingDone()) {
            lead.setMeetingsDoneCount(lead.getMeetingsDoneCount() + 1);
            changes.add("Meeting completed (Total: " + lead.getMeetingsDoneCount() + ")");
            if (lead.getLeadStage() == LeadStage.MEETING_BOOKED) {
                newStage = LeadStage.PROPOSAL_SENT.toString();
                lead.setLeadStage(LeadStage.PROPOSAL_SENT);
                changes.add("Stage changed from " + oldStage + " to " + newStage);
            }
        }

        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());
        lead.setLastContactDate(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        // Create SINGLE history entry for all statistics updates
        if (!changes.isEmpty()) {
            String allChanges = String.join(". ", changes);
            leadHistoryService.recordStatisticsUpdate(savedLead, employee, allChanges, "Statistics updated by " + employee.getEmail());
        }

        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateLeadStage(Long leadId, String stage, String employeeEmail) {
        log.info("Employee {} updating stage for lead {} to {}", employeeEmail, leadId, stage);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        String oldStage = lead.getLeadStage().toString();
        LeadStage newStage;
        try {
            newStage = LeadStage.valueOf(stage.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid lead stage: " + stage);
        }

        List<String> changes = new ArrayList<>();
        changes.add("Stage changed from " + oldStage + " to " + stage);

        lead.setLeadStage(newStage);
        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        // Auto-update statistics based on stage (without creating separate history entries)
        switch (newStage) {
            case WHATSAPP_SENT:
                lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                break;
            case CALLS_MADE:
                lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                break;
            case FOLLOW_UPS:
                lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
                break;
            case MEETINGS_BOOKED:
                lead.setMeetingsBookedCount(lead.getMeetingsBookedCount() + 1);
                break;
            case MEETINGS_DONE:
                lead.setMeetingsDoneCount(lead.getMeetingsDoneCount() + 1);
                break;
            default:
                break;
        }

        Lead savedLead = leadRepository.save(lead);

        // Create SINGLE history entry for stage change
        String allChanges = String.join(". ", changes);
        leadHistoryService.recordStageChange(savedLead, employee, oldStage, stage, allChanges);

        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateFollowUp(Long leadId, String nextFollowUpDate, String nextFollowUpDescription, String employeeEmail) {
        log.info("Employee {} updating follow-up for lead {}", employeeEmail, leadId);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        String oldFollowUpDate = lead.getNextFollowUpDate().toString();
        String oldDescription = lead.getNextFollowUp();
        LocalDate newDate = LocalDate.parse(nextFollowUpDate, DateTimeFormatter.ISO_LOCAL_DATE);

        List<String> changes = new ArrayList<>();
        changes.add("Follow-up date changed from " + oldFollowUpDate + " to " + nextFollowUpDate);
        changes.add("Follow-up description: " + nextFollowUpDescription);

        lead.setNextFollowUpDate(newDate);
        lead.setNextFollowUp(nextFollowUpDescription);
        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        Lead savedLead = leadRepository.save(lead);

        // Create SINGLE history entry for follow-up update
        String allChanges = String.join(". ", changes);
        leadHistoryService.recordFollowUpUpdate(savedLead, employee, oldFollowUpDate, nextFollowUpDate, oldDescription, nextFollowUpDescription);

        return mapToResponseDTO(savedLead);
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
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setUpdatedAt(lead.getUpdatedAt());
        dto.setUpdateCount(lead.getUpdateCount());
        dto.setLastUpdatedBy(lead.getLastUpdatedBy());
        dto.setLastContactDate(lead.getLastContactDate());

        if (lead.getAssignedEmployee() != null) {
            EmployeeResponseDTO empDto = new EmployeeResponseDTO();
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