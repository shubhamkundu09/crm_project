
// WebsiteLeadUpdateDTO.java
package com.crm.dto;

import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WebsiteLeadUpdateDTO {
    private LeadType leadType;
    private LeadStage leadStage;
    private Long assignedEmployeeId;
    private String remarks;
    private LocalDate nextFollowUpDate;
    private String nextFollowUp;
}