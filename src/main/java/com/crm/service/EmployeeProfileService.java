

// EmployeeProfileService.java
package com.crm.service;

import com.crm.dto.EmployeeProfileDTO;
import com.crm.entity.Employee;
import com.crm.exception.ResourceNotFoundException;
import com.crm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeProfileService {

    private final EmployeeRepository employeeRepository;

    public EmployeeProfileDTO getEmployeeProfileByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        return mapToProfileDTO(employee);
    }

    private EmployeeProfileDTO mapToProfileDTO(Employee employee) {
        return EmployeeProfileDTO.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .phoneNumber(employee.getPhoneNumber())
                .joiningDate(employee.getJoiningDate())
                .isActive(employee.getIsActive())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}