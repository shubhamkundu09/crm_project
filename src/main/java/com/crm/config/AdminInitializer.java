package com.crm.config;

import com.crm.entity.Employee;
import com.crm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!employeeRepository.existsByEmail("admin@crm.com")) {
            Employee admin = Employee.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@crm.com")
                    .employeeCode("ADMIN001")
                    .password(passwordEncoder.encode("Admin@123"))
                    .department("Administration")
                    .position("System Administrator")
                    .salary(0.0)
                    .phoneNumber("0000000000")
                    .joiningDate(LocalDate.now())
                    .isActive(true)
                    .isFirstLogin(false)
                    .build();
            employeeRepository.save(admin);
            log.info("Admin user created successfully");
        }
    }
}