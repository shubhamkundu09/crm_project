// EmployeeService.java
package com.crm.service;

import com.crm.dto.EmployeeDTO;
import com.crm.dto.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    void deleteEmployee(Long id);
    EmployeeResponseDTO getEmployeeById(Long id);
    List<EmployeeResponseDTO> getAllEmployees();
    List<EmployeeResponseDTO> getActiveEmployees();
    List<EmployeeResponseDTO> getEmployeesByDepartment(String department);
}