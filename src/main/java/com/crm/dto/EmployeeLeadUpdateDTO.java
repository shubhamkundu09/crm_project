package com.crm.dto;

import com.crm.entity.LeadStage;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeadUpdateDTO {

    // Contact Information
    private Boolean contactMade;
    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String responseMessage;
    private String remarks;

    // Statistics Updates
    private Boolean whatsappSent;
    private Boolean callsMade;
    private Boolean meetingBooked;
    private Boolean meetingDone;

    // Stage Update
    private LeadStage newLeadStage;

    // Follow-up Update
    @FutureOrPresent(message = "Next follow-up date must be today or in the future")
    private LocalDate nextFollowUpDate;
    private String nextFollowUpDescription;

    // Conversion
    private Boolean convertToCustomer;
}