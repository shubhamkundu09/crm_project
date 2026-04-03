package com.crm.service;

import com.crm.dto.*;
import com.crm.entity.Employee;
import com.crm.entity.Lead;
import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import com.crm.exception.DuplicateResourceException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.exception.UnauthorizedException;
import com.crm.repository.EmployeeRepository;
import com.crm.repository.LeadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final LeadHistoryService leadHistoryService;
    private final ObjectMapper objectMapper;

    @Override
    public LeadResponseDTO createLead(LeadDTO leadDTO) {
        log.info("Creating new lead with email: {}", leadDTO.getEmail());

        // Get current user from security context
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Try to find user in employee table
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail)
                .orElseGet(() -> {
                    // If admin (not in employee table), get the system admin
                    return employeeRepository.findByEmail("admin@crm.com")
                            .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
                });

        // Check for duplicate email
        if (leadRepository.existsByEmail(leadDTO.getEmail())) {
            throw new DuplicateResourceException("Lead with email already exists: " + leadDTO.getEmail());
        }

        // Get assigned employee
        Employee assignedEmployee = employeeRepository.findById(leadDTO.getAssignedEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + leadDTO.getAssignedEmployeeId()));

        Lead lead = mapToEntity(leadDTO, assignedEmployee);
        Lead savedLead = leadRepository.save(lead);

        // Record history
        leadHistoryService.recordLeadCreation(savedLead, currentUser);

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

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Lead existingLead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Track changes
        Map<String, String> changes = new HashMap<>();
        StringBuilder changesText = new StringBuilder();

        // Store old values for history
        String oldName = existingLead.getName();
        String oldEmail = existingLead.getEmail();
        String oldPhone = existingLead.getPhoneNumber();
        LeadType oldType = existingLead.getLeadType();
        LeadStage oldStage = existingLead.getLeadStage();
        LocalDate oldFollowUpDate = existingLead.getNextFollowUpDate();
        String oldRemarks = existingLead.getRemarks();
        String oldNextFollowUp = existingLead.getNextFollowUp();
        String oldSource = existingLead.getSource();
        Long oldEmployeeId = existingLead.getAssignedEmployee().getId();

        // Update fields if present
        if (leadUpdateDTO.getName() != null && !leadUpdateDTO.getName().equals(existingLead.getName())) {
            changes.put("name", "Changed from '" + existingLead.getName() + "' to '" + leadUpdateDTO.getName() + "'");
            changesText.append("Name: ").append(oldName).append(" → ").append(leadUpdateDTO.getName()).append("; ");
            existingLead.setName(leadUpdateDTO.getName());
        }

        if (leadUpdateDTO.getEmail() != null && !leadUpdateDTO.getEmail().equals(existingLead.getEmail())) {
            if (leadRepository.existsByEmail(leadUpdateDTO.getEmail())) {
                throw new DuplicateResourceException("Lead with email already exists: " + leadUpdateDTO.getEmail());
            }
            changes.put("email", "Changed from '" + existingLead.getEmail() + "' to '" + leadUpdateDTO.getEmail() + "'");
            changesText.append("Email: ").append(oldEmail).append(" → ").append(leadUpdateDTO.getEmail()).append("; ");
            existingLead.setEmail(leadUpdateDTO.getEmail());
        }

        if (leadUpdateDTO.getPhoneNumber() != null && !leadUpdateDTO.getPhoneNumber().equals(existingLead.getPhoneNumber())) {
            changes.put("phoneNumber", "Changed from '" + existingLead.getPhoneNumber() + "' to '" + leadUpdateDTO.getPhoneNumber() + "'");
            changesText.append("Phone: ").append(oldPhone).append(" → ").append(leadUpdateDTO.getPhoneNumber()).append("; ");
            existingLead.setPhoneNumber(leadUpdateDTO.getPhoneNumber());
        }

        if (leadUpdateDTO.getLeadType() != null && leadUpdateDTO.getLeadType() != existingLead.getLeadType()) {
            changes.put("leadType", "Changed from '" + existingLead.getLeadType() + "' to '" + leadUpdateDTO.getLeadType() + "'");
            changesText.append("Lead Type: ").append(oldType).append(" → ").append(leadUpdateDTO.getLeadType()).append("; ");
            existingLead.setLeadType(leadUpdateDTO.getLeadType());
        }

        if (leadUpdateDTO.getLeadStage() != null && leadUpdateDTO.getLeadStage() != existingLead.getLeadStage()) {
            String previousStage = existingLead.getLeadStage().toString();
            String newStage = leadUpdateDTO.getLeadStage().toString();
            changes.put("leadStage", "Changed from '" + previousStage + "' to '" + newStage + "'");
            changesText.append("Stage: ").append(previousStage).append(" → ").append(newStage).append("; ");
            leadHistoryService.recordStageChange(existingLead, currentUser, previousStage, newStage, "Stage updated by admin");
            existingLead.setLeadStage(leadUpdateDTO.getLeadStage());
        }

        if (leadUpdateDTO.getNextFollowUpDate() != null && !leadUpdateDTO.getNextFollowUpDate().equals(existingLead.getNextFollowUpDate())) {
            changes.put("nextFollowUpDate", "Changed from '" + existingLead.getNextFollowUpDate() + "' to '" + leadUpdateDTO.getNextFollowUpDate() + "'");
            changesText.append("Follow-up Date: ").append(oldFollowUpDate).append(" → ").append(leadUpdateDTO.getNextFollowUpDate()).append("; ");
            existingLead.setNextFollowUpDate(leadUpdateDTO.getNextFollowUpDate());
        }

        if (leadUpdateDTO.getRemarks() != null && !leadUpdateDTO.getRemarks().equals(existingLead.getRemarks())) {
            changes.put("remarks", "Remarks updated");
            changesText.append("Remarks updated; ");
            existingLead.setRemarks(leadUpdateDTO.getRemarks());
        }

        if (leadUpdateDTO.getNextFollowUp() != null && !leadUpdateDTO.getNextFollowUp().equals(existingLead.getNextFollowUp())) {
            changes.put("nextFollowUp", "Follow-up description updated");
            changesText.append("Follow-up description: ").append(oldNextFollowUp).append(" → ").append(leadUpdateDTO.getNextFollowUp()).append("; ");
            existingLead.setNextFollowUp(leadUpdateDTO.getNextFollowUp());
        }

        if (leadUpdateDTO.getSource() != null && !leadUpdateDTO.getSource().equals(existingLead.getSource())) {
            changes.put("source", "Changed from '" + existingLead.getSource() + "' to '" + leadUpdateDTO.getSource() + "'");
            changesText.append("Source: ").append(oldSource).append(" → ").append(leadUpdateDTO.getSource()).append("; ");
            existingLead.setSource(leadUpdateDTO.getSource());
        }

        if (leadUpdateDTO.getAssignedEmployeeId() != null && !leadUpdateDTO.getAssignedEmployeeId().equals(existingLead.getAssignedEmployee().getId())) {
            Employee newEmployee = employeeRepository.findById(leadUpdateDTO.getAssignedEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + leadUpdateDTO.getAssignedEmployeeId()));
            changes.put("assignedEmployee", "Changed from '" + existingLead.getAssignedEmployee().getEmail() + "' to '" + newEmployee.getEmail() + "'");
            changesText.append("Assigned Employee: ").append(oldEmployeeId).append(" → ").append(leadUpdateDTO.getAssignedEmployeeId()).append("; ");
            existingLead.setAssignedEmployee(newEmployee);

            // Send notification to new assigned employee
            emailService.sendLeadAssignmentEmail(
                    newEmployee.getEmail(),
                    newEmployee.getFirstName() + " " + newEmployee.getLastName(),
                    existingLead.getName(),
                    existingLead.getLeadType().getDescription()
            );
        }

        existingLead.setUpdateCount(existingLead.getUpdateCount() + 1);
        existingLead.setLastUpdatedBy(currentUser.getEmail());

        Lead updatedLead = leadRepository.save(existingLead);

        // Record history if there are changes
        if (!changes.isEmpty()) {
            String finalChanges = changesText.toString();
            leadHistoryService.recordLeadUpdate(updatedLead, currentUser, finalChanges, "Lead details updated by " + currentUser.getEmail());
        }

        log.info("Lead updated successfully with ID: {}", id);
        return mapToResponseDTO(updatedLead);
    }

    @Override
    public void deleteLead(Long id) {
        log.info("Deleting lead with ID: {}", id);

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Record deletion in history before soft delete
        leadHistoryService.recordLeadUpdate(lead, currentUser, "Lead marked as inactive", "Lead deleted by " + currentUser.getEmail());

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

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee currentUser = employeeRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Verify employee has access to this lead
        boolean isAdmin = currentUserEmail.equals("admin") || currentUser.getEmail().equals("admin");
        boolean isAssignedEmployee = lead.getAssignedEmployee() != null && lead.getAssignedEmployee().getId().equals(currentUser.getId());

        if (isAdmin || isAssignedEmployee) {

            Map<String, String> statsChanges = new HashMap<>();
            StringBuilder statsText = new StringBuilder();

            // Store old values
            int oldWhatsappCount = lead.getWhatsappSentCount() != null ? lead.getWhatsappSentCount() : 0;
            int oldCallsCount = lead.getCallsMadeCount() != null ? lead.getCallsMadeCount() : 0;
            int oldFollowupsCount = lead.getFollowUpsCount() != null ? lead.getFollowUpsCount() : 0;
            int oldMeetingsBooked = lead.getMeetingsBookedCount() != null ? lead.getMeetingsBookedCount() : 0;
            int oldMeetingsDone = lead.getMeetingsDoneCount() != null ? lead.getMeetingsDoneCount() : 0;
            LeadStage oldStage = lead.getLeadStage();

            if (statisticsDTO.getWhatsappSent() != null && statisticsDTO.getWhatsappSent()) {
                lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                statsChanges.put("whatsappSent", "WhatsApp sent count increased from " + oldWhatsappCount + " to " + lead.getWhatsappSentCount());
                statsText.append("WhatsApp Sent: ").append(oldWhatsappCount).append(" → ").append(lead.getWhatsappSentCount()).append("; ");
                lead.setLastContactDate(LocalDateTime.now());
                leadHistoryService.recordContactMade(lead, currentUser, "WhatsApp", "WhatsApp message sent", "WhatsApp communication sent to lead");
            }
            if (statisticsDTO.getCallsMade() != null && statisticsDTO.getCallsMade()) {
                lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                statsChanges.put("callsMade", "Calls made count increased from " + oldCallsCount + " to " + lead.getCallsMadeCount());
                statsText.append("Calls Made: ").append(oldCallsCount).append(" → ").append(lead.getCallsMadeCount()).append("; ");
                lead.setLastContactDate(LocalDateTime.now());
                leadHistoryService.recordContactMade(lead, currentUser, "Phone Call", "Phone call made to lead", "Phone conversation completed");
            }
            if (statisticsDTO.getFollowUp() != null && statisticsDTO.getFollowUp()) {
                lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
                statsChanges.put("followUp", "Follow-ups count increased from " + oldFollowupsCount + " to " + lead.getFollowUpsCount());
                statsText.append("Follow-ups: ").append(oldFollowupsCount).append(" → ").append(lead.getFollowUpsCount()).append("; ");
            }
            if (statisticsDTO.getMeetingBooked() != null && statisticsDTO.getMeetingBooked()) {
                lead.setMeetingsBookedCount(lead.getMeetingsBookedCount() + 1);
                statsChanges.put("meetingBooked", "Meetings booked count increased from " + oldMeetingsBooked + " to " + lead.getMeetingsBookedCount());
                statsText.append("Meetings Booked: ").append(oldMeetingsBooked).append(" → ").append(lead.getMeetingsBookedCount()).append("; ");

                // Record stage change
                if (lead.getLeadStage() != LeadStage.MEETING_BOOKED) {
                    leadHistoryService.recordStageChange(lead, currentUser, oldStage.toString(), LeadStage.MEETING_BOOKED.toString(), "Meeting booked with lead");
                    lead.setLeadStage(LeadStage.MEETING_BOOKED);
                }
            }
            if (statisticsDTO.getMeetingDone() != null && statisticsDTO.getMeetingDone()) {
                lead.setMeetingsDoneCount(lead.getMeetingsDoneCount() + 1);
                statsChanges.put("meetingDone", "Meetings done count increased from " + oldMeetingsDone + " to " + lead.getMeetingsDoneCount());
                statsText.append("Meetings Done: ").append(oldMeetingsDone).append(" → ").append(lead.getMeetingsDoneCount()).append("; ");

                // Record stage change
                if (lead.getLeadStage() == LeadStage.MEETING_BOOKED) {
                    leadHistoryService.recordStageChange(lead, currentUser, lead.getLeadStage().toString(), LeadStage.PROPOSAL_SENT.toString(), "Meeting completed, moving to proposal stage");
                    lead.setLeadStage(LeadStage.PROPOSAL_SENT);
                }
            }

            lead.setUpdateCount(lead.getUpdateCount() + 1);
            lead.setLastUpdatedBy(currentUser.getEmail());

            Lead updatedLead = leadRepository.save(lead);

            if (!statsChanges.isEmpty()) {
                leadHistoryService.recordStatisticsUpdate(updatedLead, currentUser, statsText.toString(), "Statistics updated by " + currentUser.getEmail());
            }

            log.info("Statistics updated for lead {} by {}", id, currentUser.getEmail());
            return mapToResponseDTO(updatedLead);
        } else {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }
    }

    @Override
    public Map<String, Long> getLeadStatistics() {
        log.info("Fetching lead statistics");

        Map<String, Long> statistics = new HashMap<>();

        List<Object[]> typeStats = leadRepository.countLeadsByType();
        for (Object[] stat : typeStats) {
            statistics.put("type_" + stat[0].toString().toLowerCase(), (Long) stat[1]);
        }

        List<Object[]> stageStats = leadRepository.countLeadsByStage();
        for (Object[] stat : stageStats) {
            statistics.put("stage_" + stat[0].toString().toLowerCase(), (Long) stat[1]);
        }

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

    @Override
    public LeadResponseDTO updateLeadStage(Long id, String stage, String employeeEmail) {
        log.info("Updating lead stage for lead ID: {} to {}", id, stage);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Verify ownership
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
            case CONVERSIONS:
                lead.setLeadStage(LeadStage.CLOSED);
                log.info("Lead {} converted to customer", id);
                break;
            default:
                break;
        }

        Lead savedLead = leadRepository.save(lead);

        // Record stage change in history
        leadHistoryService.recordStageChange(savedLead, employee, oldStage, stage, "Stage updated by " + employee.getEmail());

        log.info("Stage updated for lead {} to {} by {}", id, newStage, employeeEmail);
        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateFollowUp(Long id, String nextFollowUpDate, String nextFollowUpDescription, String employeeEmail) {
        log.info("Updating follow-up for lead ID: {}", id);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Verify ownership
        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        String oldFollowUpDate = lead.getNextFollowUpDate().toString();
        String oldDescription = lead.getNextFollowUp();
        LocalDate newDate = LocalDate.parse(nextFollowUpDate);

        lead.setNextFollowUpDate(newDate);
        lead.setNextFollowUp(nextFollowUpDescription);
        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        // Update stage to follow-ups if not already in appropriate stage
        if (lead.getLeadStage() != LeadStage.FOLLOW_UPS &&
                lead.getLeadStage() != LeadStage.MEETING_BOOKED &&
                lead.getLeadStage() != LeadStage.NEGOTIATION) {
            String oldStage = lead.getLeadStage().toString();
            lead.setLeadStage(LeadStage.FOLLOW_UPS);
            leadHistoryService.recordStageChange(lead, employee, oldStage, LeadStage.FOLLOW_UPS.toString(), "Moving to follow-up stage");
        }

        Lead savedLead = leadRepository.save(lead);

        // Record follow-up update
        leadHistoryService.recordFollowUpUpdate(savedLead, employee, oldFollowUpDate, nextFollowUpDate, oldDescription, nextFollowUpDescription);

        log.info("Follow-up updated for lead {} for date {} by {}", id, newDate, employeeEmail);
        return mapToResponseDTO(savedLead);
    }

    @Override
    public LeadResponseDTO updateLeadAfterContact(Long id, EmployeeLeadUpdateDTO updateDTO, String employeeEmail) {
        log.info("Updating lead after contact for lead ID: {}", id);

        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));

        // Verify ownership
        if (!lead.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new UnauthorizedException("You are not authorized to update this lead");
        }

        StringBuilder contactInfo = new StringBuilder();

        // Update based on contact made
        if (Boolean.TRUE.equals(updateDTO.getContactMade())) {
            contactInfo.append("Contact made. ");

            // Increment appropriate statistics based on contact type
            if (updateDTO.getResponseMessage() != null) {
                if (updateDTO.getResponseMessage().toLowerCase().contains("call")) {
                    lead.setCallsMadeCount(lead.getCallsMadeCount() + 1);
                    contactInfo.append("Phone call made. ");
                    leadHistoryService.recordContactMade(lead, employee, "Phone", updateDTO.getResponseMessage(), "Call completed");
                } else if (updateDTO.getResponseMessage().toLowerCase().contains("whatsapp")) {
                    lead.setWhatsappSentCount(lead.getWhatsappSentCount() + 1);
                    contactInfo.append("WhatsApp message sent. ");
                    leadHistoryService.recordContactMade(lead, employee, "WhatsApp", updateDTO.getResponseMessage(), "WhatsApp communication sent");
                }
                lead.setFollowUpsCount(lead.getFollowUpsCount() + 1);
                contactInfo.append("Follow-up recorded. ");
            }

            lead.setLastContactDate(LocalDateTime.now());
        }

        // Update lead stage if provided
        if (updateDTO.getNewLeadStage() != null) {
            String oldStage = lead.getLeadStage().toString();
            lead.setLeadStage(updateDTO.getNewLeadStage());
            contactInfo.append("Stage changed from ").append(oldStage).append(" to ").append(updateDTO.getNewLeadStage()).append(". ");
            leadHistoryService.recordStageChange(lead, employee, oldStage, updateDTO.getNewLeadStage().toString(), updateDTO.getResponseMessage());
        }

        // Update remarks
        if (updateDTO.getRemarks() != null) {
            lead.setRemarks(updateDTO.getRemarks());
            contactInfo.append("Remarks updated. ");
        }

        // Handle conversion to customer
        if (Boolean.TRUE.equals(updateDTO.getConvertToCustomer())) {
            String oldStage = lead.getLeadStage().toString();
            lead.setLeadStage(LeadStage.CLOSED);
            contactInfo.append("Lead converted to customer! ");
            leadHistoryService.recordStageChange(lead, employee, oldStage, LeadStage.CLOSED.toString(), "Lead converted to customer");
            log.info("Lead {} marked as converted to customer by {}", id, employeeEmail);
        }

        lead.setUpdateCount(lead.getUpdateCount() + 1);
        lead.setLastUpdatedBy(employee.getEmail());

        Lead savedLead = leadRepository.save(lead);

        // Record contact made
        if (contactInfo.length() > 0) {
            leadHistoryService.recordContactMade(savedLead, employee, "Contact", contactInfo.toString(), updateDTO.getResponseMessage());
        }

        log.info("Lead {} updated after contact by {}", id, employeeEmail);
        return mapToResponseDTO(savedLead);
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
                .updateCount(0)
                .lastUpdatedBy("SYSTEM")
                .whatsappSentCount(0)
                .callsMadeCount(0)
                .followUpsCount(0)
                .meetingsBookedCount(0)
                .meetingsDoneCount(0)
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
                .updateCount(lead.getUpdateCount())
                .lastUpdatedBy(lead.getLastUpdatedBy())
                .lastContactDate(lead.getLastContactDate())
                .build();
    }

    private EmployeeResponseDTO mapToEmployeeResponseDTO(Employee employee) {
        if (employee == null) return null;
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