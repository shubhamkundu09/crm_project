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

        StringBuilder changes = new StringBuilder();

        if (Boolean.TRUE.equals(updateDTO.getContactMade())) {
            changes.append("Contact made. ");

            if (updateDTO.getResponseMessage() != null) {
                if (updateDTO.getResponseMessage().toLowerCase().contains("call")) {
                    lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                    changes.append("Call recorded. ");
                    leadHistoryService.recordContactMade(lead, employee, "Phone", updateDTO.getResponseMessage(), "Phone call completed");
                } else if (updateDTO.getResponseMessage().toLowerCase().contains("whatsapp")) {
                    lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                    changes.append("WhatsApp sent. ");
                    leadHistoryService.recordContactMade(lead, employee, "WhatsApp", updateDTO.getResponseMessage(), "WhatsApp message sent");
                }
                lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
                changes.append("Follow-up recorded. ");
            }
            lead.setLastContactDate(LocalDateTime.now());
        }

        if (updateDTO.getNewLeadStage() != null && updateDTO.getNewLeadStage() != lead.getLeadStage()) {
            String oldStage = lead.getLeadStage().toString();
            lead.setLeadStage(updateDTO.getNewLeadStage());
            changes.append("Stage changed from ").append(oldStage).append(" to ").append(updateDTO.getNewLeadStage()).append(". ");
            leadHistoryService.recordStageChange(lead, employee, oldStage, updateDTO.getNewLeadStage().toString(), updateDTO.getResponseMessage());
        }

        if (updateDTO.getRemarks() != null) {
            lead.setRemarks(updateDTO.getRemarks());
            changes.append("Remarks updated. ");
        }

        if (Boolean.TRUE.equals(updateDTO.getConvertToCustomer())) {
            String oldStage = lead.getLeadStage().toString();
            lead.setLeadStage(LeadStage.CLOSED);
            changes.append("Lead converted to customer! ");
            leadHistoryService.recordStageChange(lead, employee, oldStage, LeadStage.CLOSED.toString(), "Lead converted to customer");
        }

        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        Lead savedLead = leadRepository.save(lead);

        if (changes.length() > 0) {
            leadHistoryService.recordLeadUpdate(savedLead, employee, changes.toString(), "Updated by employee");
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

        StringBuilder statsChanges = new StringBuilder();

        if (statisticsDTO.getWhatsappSent() != null && statisticsDTO.getWhatsappSent()) {
            lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
            statsChanges.append("WhatsApp sent count: ").append(lead.getWhatsappSentCount()).append(". ");
            leadHistoryService.recordContactMade(lead, employee, "WhatsApp", "WhatsApp message sent", "WhatsApp communication sent");
        }
        if (statisticsDTO.getCallsMade() != null && statisticsDTO.getCallsMade()) {
            lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
            statsChanges.append("Calls made count: ").append(lead.getCallsMadeCount()).append(". ");
            leadHistoryService.recordContactMade(lead, employee, "Phone Call", "Phone call made", "Phone conversation completed");
        }
        if (statisticsDTO.getFollowUp() != null && statisticsDTO.getFollowUp()) {
            lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
            statsChanges.append("Follow-ups count: ").append(lead.getFollowUpsCount()).append(". ");
        }
        if (statisticsDTO.getMeetingBooked() != null && statisticsDTO.getMeetingBooked()) {
            lead.setMeetingsBookedCount(lead.getMeetingsBookedCount() + 1);
            statsChanges.append("Meetings booked count: ").append(lead.getMeetingsBookedCount()).append(". ");
            if (lead.getLeadStage() != LeadStage.MEETING_BOOKED) {
                String oldStage = lead.getLeadStage().toString();
                lead.setLeadStage(LeadStage.MEETING_BOOKED);
                leadHistoryService.recordStageChange(lead, employee, oldStage, LeadStage.MEETING_BOOKED.toString(), "Meeting booked with lead");
            }
        }
        if (statisticsDTO.getMeetingDone() != null && statisticsDTO.getMeetingDone()) {
            lead.setMeetingsDoneCount(lead.getMeetingsDoneCount() + 1);
            statsChanges.append("Meetings done count: ").append(lead.getMeetingsDoneCount()).append(". ");
            if (lead.getLeadStage() == LeadStage.MEETING_BOOKED) {
                String oldStage = lead.getLeadStage().toString();
                lead.setLeadStage(LeadStage.PROPOSAL_SENT);
                leadHistoryService.recordStageChange(lead, employee, oldStage, LeadStage.PROPOSAL_SENT.toString(), "Meeting completed, moving to proposal stage");
            }
        }

        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());
        lead.setLastContactDate(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        if (statsChanges.length() > 0) {
            leadHistoryService.recordStatisticsUpdate(savedLead, employee, statsChanges.toString(), "Statistics updated by " + employee.getEmail());
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

        lead.setLeadStage(newStage);
        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        // Record stage change
        leadHistoryService.recordStageChange(lead, employee, oldStage, stage, "Stage updated by employee");

        // Auto-update statistics based on stage
        switch (newStage) {
            case WHATSAPP_SENT:
                lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                leadHistoryService.recordContactMade(lead, employee, "WhatsApp", "WhatsApp message sent", "WhatsApp communication sent");
                break;
            case CALLS_MADE:
                lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                leadHistoryService.recordContactMade(lead, employee, "Phone Call", "Phone call made", "Phone conversation completed");
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

        lead.setNextFollowUpDate(newDate);
        lead.setNextFollowUp(nextFollowUpDescription);
        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        // Record follow-up update
        leadHistoryService.recordFollowUpUpdate(lead, employee, oldFollowUpDate, nextFollowUpDate, oldDescription, nextFollowUpDescription);

        Lead savedLead = leadRepository.save(lead);
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