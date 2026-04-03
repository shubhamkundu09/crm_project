package com.crm.repository;

import com.crm.entity.LeadHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadHistoryRepository extends JpaRepository<LeadHistory, Long> {

    List<LeadHistory> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    List<LeadHistory> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT lh FROM LeadHistory lh WHERE lh.lead.id = :leadId AND lh.action = :action ORDER BY lh.createdAt DESC")
    List<LeadHistory> findByLeadIdAndAction(@Param("leadId") Long leadId, @Param("action") String action);

    @Query("SELECT lh FROM LeadHistory lh WHERE lh.lead.assignedEmployee.id = :employeeId ORDER BY lh.createdAt DESC")
    List<LeadHistory> findByAssignedEmployeeId(@Param("employeeId") Long employeeId);
}