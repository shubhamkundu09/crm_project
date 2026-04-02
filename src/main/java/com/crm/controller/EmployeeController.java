// EmployeeController.java (updated)
package com.crm.controller;

import com.crm.dto.ApiResponse;
import com.crm.dto.EmployeeDTO;
import com.crm.dto.EmployeeResponseDTO;
import com.crm.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeDTO employeeDTO,
            HttpServletRequest request) {
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdEmployee, "Employee created successfully", request.getRequestURI()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO,
            HttpServletRequest request) {
        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedEmployee, "Employee updated successfully", request.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id,
            HttpServletRequest request) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", request.getRequestURI()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(
            @PathVariable Long id,
            HttpServletRequest request) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee, "Employee retrieved successfully", request.getRequestURI()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getAllEmployees(HttpServletRequest request) {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success(employees, "Employees retrieved successfully", request.getRequestURI()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getActiveEmployees(HttpServletRequest request) {
        List<EmployeeResponseDTO> employees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(ApiResponse.success(employees, "Active employees retrieved successfully", request.getRequestURI()));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getEmployeesByDepartment(
            @PathVariable String department,
            HttpServletRequest request) {
        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(employees, "Employees retrieved successfully for department: " + department, request.getRequestURI()));
    }
}