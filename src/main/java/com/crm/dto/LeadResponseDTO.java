
// LeadResponseDTO.java
package com.crm.dto;

import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private LeadType leadType;
    private LeadStage leadStage;
    private LocalDate nextFollowUpDate;
    private String remarks;
    private String nextFollowUp;
    private EmployeeResponseDTO assignedEmployee;
    private String source;
    private Boolean isActive;

    // Statistics
    private Integer whatsappSentCount;
    private Integer callsMadeCount;
    private Integer followUpsCount;
    private Integer meetingsBookedCount;
    private Integer meetingsDoneCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
