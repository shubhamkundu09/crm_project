package com.crm.config;

import com.crm.entity.Employee;
import com.crm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.first-name}")
    private String adminFirstName;

    @Value("${app.admin.last-name}")
    private String adminLastName;

    @Value("${app.admin.employee-code}")
    private String adminEmployeeCode;

    @Value("${app.admin.department}")
    private String adminDepartment;

    @Value("${app.admin.position}")
    private String adminPosition;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists (check by email)
        if (!employeeRepository.existsByEmail(adminEmail)) {
            Employee admin = Employee.builder()
                    .firstName(adminFirstName)
                    .lastName(adminLastName)
                    .email(adminEmail)
                    .employeeCode(adminEmployeeCode)
                    .password(passwordEncoder.encode(adminPassword))
                    .department(adminDepartment)
                    .position(adminPosition)
                    .salary(0.0)
                    .phoneNumber("0000000000")
                    .joiningDate(LocalDate.now())
                    .isActive(true)
                    .isFirstLogin(false)
                    .build();
            employeeRepository.save(admin);
            log.info("Admin user created successfully with email: {}", adminEmail);
        } else {
            log.info("Admin user already exists with email: {}", adminEmail);
        }
    }
}