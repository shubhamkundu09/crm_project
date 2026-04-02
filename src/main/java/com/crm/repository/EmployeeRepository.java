package com.crm.repository;

import com.crm.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByDepartment(String department);

    List<Employee> findByIsActiveTrue();

    List<Employee> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);

    @Query("SELECT e FROM Employee e WHERE e.department = :dept AND e.isActive = true")
    List<Employee> findActiveEmployeesByDepartment(@Param("dept") String department);

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);
}