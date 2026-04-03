// LeadDTO.java
package com.crm.dto;

import com.crm.entity.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {

    private Long id;

    @NotBlank(message = "Lead name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Lead type is required")
    private LeadType leadType;

    @NotNull(message = "Lead stage is required")
    private LeadStage leadStage;

    @NotNull(message = "Next follow-up date is required")
    @FutureOrPresent(message = "Next follow-up date must be today or in the future")
    private LocalDate nextFollowUpDate;

    @NotBlank(message = "Remarks are required")
    private String remarks;

    @NotBlank(message = "Next follow-up description is required")
    private String nextFollowUp;

    @NotNull(message = "Assigned employee ID is required")
    private Long assignedEmployeeId;

    @NotBlank(message = "Source is required")
    private String source;


    // Add these fields to LeadDTO class
    private MainService interestedService;
    private ServiceSubcategory serviceSubcategory;
    private ServiceSubSubcategory serviceSubSubcategory;
    private String serviceDescription;
}
