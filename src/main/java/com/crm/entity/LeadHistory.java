package com.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lead_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, STAGE_CHANGE, STATISTICS_UPDATE, FOLLOWUP_UPDATE, CONTACT_MADE

    @Column(length = 2000)
    private String changes; // JSON or text description of changes

    @Column(length = 1000)
    private String remarks;

    @Column(nullable = false)
    private String previousStage;

    @Column(nullable = false)
    private String newStage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}