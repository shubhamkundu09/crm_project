
// EmployeeResponseDTO.java
package com.crm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeCode;
    private String department;
    private String position;
    private Double salary;
    private String phoneNumber;
    private LocalDateTime joiningDate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}