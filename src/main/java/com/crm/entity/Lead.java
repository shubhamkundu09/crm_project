package com.crm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadType leadType;  // HOT, WARM, COLD

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadStage leadStage;  // LEAD_GENERATED, CONTACTED, INTERESTED, MEETING_BOOKED, PROPOSAL_SENT, NEGOTIATION, CLOSED

    @Column(nullable = false)
    private LocalDate nextFollowUpDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String remarks;

    @Column(nullable = false)
    private String nextFollowUp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id", nullable = false)
    private Employee assignedEmployee;

    @Column(nullable = false)
    private String source;  // Where the lead came from

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Lead Statistics
    private Integer whatsappSentCount;
    private Integer callsMadeCount;
    private Integer followUpsCount;
    private Integer meetingsBookedCount;
    private Integer meetingsDoneCount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void initializeCounts() {
        if (whatsappSentCount == null) whatsappSentCount = 0;
        if (callsMadeCount == null) callsMadeCount = 0;
        if (followUpsCount == null) followUpsCount = 0;
        if (meetingsBookedCount == null) meetingsBookedCount = 0;
        if (meetingsDoneCount == null) meetingsDoneCount = 0;
    }
}