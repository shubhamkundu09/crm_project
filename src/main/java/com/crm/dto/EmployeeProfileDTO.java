package com.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;  // Add this import
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeCode;
    private String department;
    private String position;
    private Double salary;
    private String phoneNumber;
    private LocalDate joiningDate;  // Changed from LocalDateTime to LocalDate
    private Boolean isActive;
    private LocalDateTime createdAt;
}