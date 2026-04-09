package com.crm.service;

import com.crm.dto.ChangePasswordRequest;
import com.crm.dto.EmployeeDTO;
import com.crm.dto.EmployeeResponseDTO;
import com.crm.dto.PasswordResetDTO;
import com.crm.entity.Employee;
import com.crm.exception.DuplicateResourceException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "redcircle0908@gmail.com";

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee with email: {}", employeeDTO.getEmail());

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + employeeDTO.getEmail());
        }

        if (!isValidPassword(employeeDTO.getPassword())) {
            throw new IllegalArgumentException("Password does not meet security requirements. " +
                    "It must contain at least 8 characters, one digit, one lowercase, one uppercase, and one special character");
        }

        Employee employee = mapToEntity(employeeDTO);
        employee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        employee.setIsFirstLogin(true);
        employee.setIsActive(true);

        Employee savedEmployee = employeeRepository.save(employee);
        String empCode = String.format("EMP%05d", savedEmployee.getId());
        savedEmployee.setEmployeeCode(empCode);
        employeeRepository.save(savedEmployee);

        emailService.sendWelcomeEmail(
                savedEmployee.getEmail(),
                savedEmployee.getFirstName() + " " + savedEmployee.getLastName(),
                savedEmployee.getEmployeeCode(),
                employeeDTO.getPassword()
        );

        log.info("Employee created successfully with ID: {} and Code: {}", savedEmployee.getId(), savedEmployee.getEmployeeCode());
        return mapToResponseDTO(savedEmployee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        log.info("Updating employee with ID: {}", id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (!existingEmployee.getEmail().equals(employeeDTO.getEmail()) &&
                employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + employeeDTO.getEmail());
        }

        existingEmployee.setFirstName(employeeDTO.getFirstName());
        existingEmployee.setLastName(employeeDTO.getLastName());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setDepartment(employeeDTO.getDepartment());
        existingEmployee.setPosition(employeeDTO.getPosition());
        existingEmployee.setSalary(employeeDTO.getSalary());
        existingEmployee.setPhoneNumber(employeeDTO.getPhoneNumber());
        existingEmployee.setJoiningDate(employeeDTO.getJoiningDate());

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().trim().isEmpty()) {
            if (!isValidPassword(employeeDTO.getPassword())) {
                throw new IllegalArgumentException("Password does not meet security requirements");
            }
            existingEmployee.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
            existingEmployee.setIsFirstLogin(true);
            log.info("Password updated for employee ID: {}", id);
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee updated successfully with ID: {}", id);

        return mapToResponseDTO(updatedEmployee);
    }

    @Override
    public void deactivateEmployee(Long id) {
        log.info("Deactivating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        // Prevent deactivating main admin
        if (ADMIN_EMAIL.equals(employee.getEmail())) {
            throw new IllegalStateException("Cannot deactivate the main admin account");
        }

        // Check if already deactivated
        if (!employee.getIsActive()) {
            log.warn("Employee with ID: {} is already deactivated", id);
            throw new IllegalStateException("Employee is already deactivated");
        }

        employee.setIsActive(false);
        employeeRepository.save(employee);

        log.info("Employee deactivated successfully with ID: {}", id);
    }

    @Override
    public void permanentlyDeleteEmployee(Long id) {
        log.info("Permanently deleting employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        // Prevent deleting main admin
        if (ADMIN_EMAIL.equals(employee.getEmail())) {
            throw new IllegalStateException("Cannot permanently delete the main admin account");
        }

        // Hard delete from database
        employeeRepository.delete(employee);

        log.info("Employee permanently deleted with ID: {}", id);
    }

    @Override
    public EmployeeResponseDTO reactivateEmployee(Long id) {
        log.info("Reactivating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (employee.getIsActive()) {
            log.warn("Employee with ID: {} is already active", id);
            throw new IllegalStateException("Employee is already active");
        }

        employee.setIsActive(true);
        Employee reactivatedEmployee = employeeRepository.save(employee);

        log.info("Employee reactivated successfully with ID: {}", id);
        return mapToResponseDTO(reactivatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        // Legacy method - maps to deactivate
        deactivateEmployee(id);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        return mapToResponseDTO(employee);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {
        log.info("Fetching active employees");
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> getDeactivatedEmployees() {
        log.info("Fetching deactivated employees");
        return employeeRepository.findByIsActiveFalse()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByDepartment(String department) {
        log.info("Fetching employees from department: {}", department);
        return employeeRepository.findByDepartment(department)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public void changePassword(String email, ChangePasswordRequest passwordRequest) {
        log.info("Changing password for employee: {}", email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));

        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (!isValidPassword(passwordRequest.getNewPassword())) {
            throw new IllegalArgumentException("Password does not meet security requirements. " +
                    "It must contain at least 8 characters, one digit, one lowercase, one uppercase, and one special character");
        }

        // Update password
        employee.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        employee.setIsFirstLogin(false);
        employeeRepository.save(employee);

        // Send confirmation email
        emailService.sendPasswordChangeConfirmation(employee.getEmail(),
                employee.getFirstName() + " " + employee.getLastName());

        log.info("Password changed successfully for employee: {}", email);
    }

    @Override
    public void resetEmployeePassword(PasswordResetDTO passwordResetDTO) {
        log.info("Resetting password for employee: {}", passwordResetDTO.getEmail());

        Employee employee = employeeRepository.findByEmail(passwordResetDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + passwordResetDTO.getEmail()));

        if (!isValidPassword(passwordResetDTO.getNewPassword())) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        String encodedPassword = passwordEncoder.encode(passwordResetDTO.getNewPassword());
        employee.setPassword(encodedPassword);
        employee.setIsFirstLogin(true);
        employeeRepository.save(employee);

        emailService.sendPasswordResetEmail(
                employee.getEmail(),
                employee.getFirstName() + " " + employee.getLastName(),
                passwordResetDTO.getNewPassword()
        );

        log.info("Password reset successfully for employee: {}", passwordResetDTO.getEmail());
    }

    @Override
    public Page<Employee> searchEmployees(String empCode, String name, String department, Boolean isActive, Pageable pageable) {
        empCode = normalize(empCode);
        name = normalize(name);
        department = normalize(department);
        return employeeRepository.searchEmployees(empCode, name, department, isActive, pageable);
    }

    private String normalize(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        return password != null && password.matches(passwordPattern);
    }

    private Employee mapToEntity(EmployeeDTO dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .salary(dto.getSalary())
                .phoneNumber(dto.getPhoneNumber())
                .joiningDate(dto.getJoiningDate())
                .isActive(true)
                .isFirstLogin(true)
                .build();
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        return EmployeeResponseDTO.builder()
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
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}