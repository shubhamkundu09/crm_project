package com.crm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeadHistoryDTO {
    private Long id;
    private String action;
    private String changes;
    private String remarks;
    private String previousStage;
    private String newStage;
    private LocalDateTime createdAt;
    private EmployeeBasicDTO employee;
}