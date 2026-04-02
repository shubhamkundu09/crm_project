// EmailService.java (updated)
package com.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Async
    public void sendWelcomeEmail(String to, String name, String employeeCode, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to CRM System - Your Account Details");
            message.setText(String.format("""
                Dear %s,
                
                Welcome to the CRM System! Your employee account has been created successfully.
                
                Your Account Details:
                ------------------------
                Employee Code: %s
                Email: %s
                Temporary Password: %s
                
                Important Notes:
                1. Please change your password after first login
                2. Never share your password with anyone
                3. For security reasons, you will be prompted to change your password on first login
                
                You can log in to the system using your email and the temporary password.
                
                Best regards,
                CRM Admin Team
                """, name, employeeCode, to, password));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("CRM System - Password Reset Notification");
            message.setText(String.format("""
                Dear %s,
                
                Your password has been reset by the administrator.
                
                New Login Credentials:
                ---------------------
                Email: %s
                New Password: %s
                
                Security Instructions:
                1. Please change this password after logging in
                2. Do not share this password with anyone
                3. If you didn't request this reset, please contact the administrator immediately
                
                Best regards,
                CRM Admin Team
                """, name, to, newPassword));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }


    // Add this method to EmailService.java
    @Async
    public void sendLeadAssignmentEmail(String to, String employeeName, String leadName, String leadType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("New Lead Assigned - CRM System");
            message.setText(String.format("""
            Dear %s,
            
            A new lead has been assigned to you.
            
            Lead Details:
            -------------
            Name: %s
            Type: %s
            
            Please log in to the CRM system to view more details and take necessary action.
            
            Best regards,
            CRM Admin Team
            """, employeeName, leadName, leadType));

            mailSender.send(message);
            log.info("Lead assignment email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send lead assignment email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordChangeConfirmation(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("CRM System - Password Changed Successfully");
            message.setText(String.format("""
                Dear %s,
                
                This is to confirm that your password has been successfully changed.
                
                If you did not make this change, please contact the system administrator immediately at %s.
                
                Best regards,
                CRM Admin Team
                """, name, adminEmail));

            mailSender.send(message);
            log.info("Password change confirmation sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation to {}: {}", to, e.getMessage());
        }
    }
}