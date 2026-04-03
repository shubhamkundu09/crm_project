package com.crm.dto;

import com.crm.entity.LeadStage;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeadUpdateDTO {

    @Size(max = 1000, message = "Response cannot exceed 1000 characters")
    private String responseMessage;

    private Boolean contactMade;

    private LeadStage newLeadStage;

    private String remarks;

    private Boolean convertToCustomer;
}