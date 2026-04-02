package com.crm.repository;

import com.crm.entity.Lead;
import com.crm.entity.LeadStage;
import com.crm.entity.LeadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByAssignedEmployeeId(Long employeeId);

    List<Lead> findByLeadType(LeadType leadType);

    List<Lead> findByLeadStage(LeadStage leadStage);

    List<Lead> findByNextFollowUpDate(LocalDate date);

    List<Lead> findByNextFollowUpDateBefore(LocalDate date);

    List<Lead> findByIsActiveTrue();

    @Query("SELECT l FROM Lead l WHERE l.assignedEmployee.id = :employeeId AND l.nextFollowUpDate = :date")
    List<Lead> findFollowUpsByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT l FROM Lead l WHERE l.leadStage IN :stages")
    List<Lead> findByLeadStages(@Param("stages") List<LeadStage> stages);

    boolean existsByEmail(String email);

    long countByLeadType(LeadType leadType);

    long countByLeadStage(LeadStage leadStage);

    @Query("SELECT l.leadStage, COUNT(l) FROM Lead l GROUP BY l.leadStage")
    List<Object[]> countLeadsByStage();

    @Query("SELECT l.leadType, COUNT(l) FROM Lead l GROUP BY l.leadType")
    List<Object[]> countLeadsByType();
}