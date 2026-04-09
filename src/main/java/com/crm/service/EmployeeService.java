package com.crm.service;

import com.crm.dto.ChangePasswordRequest;
import com.crm.dto.EmployeeDTO;
import com.crm.dto.EmployeeResponseDTO;
import com.crm.dto.PasswordResetDTO;
import com.crm.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeDTO employeeDTO);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeDTO employeeDTO);
    void deactivateEmployee(Long id);
    void permanentlyDeleteEmployee(Long id);
    EmployeeResponseDTO reactivateEmployee(Long id);
    void deleteEmployee(Long id);
    EmployeeResponseDTO getEmployeeById(Long id);
    List<EmployeeResponseDTO> getAllEmployees();
    List<EmployeeResponseDTO> getActiveEmployees();
    List<EmployeeResponseDTO> getDeactivatedEmployees();
    List<EmployeeResponseDTO> getEmployeesByDepartment(String department);
    void resetEmployeePassword(PasswordResetDTO passwordResetDTO);
    void changePassword(String email, ChangePasswordRequest passwordRequest);  // Add this method
    Page<Employee> searchEmployees(String empCode, String name, String department, Boolean isActive, Pageable pageable);
}