package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    boolean existsByUserId(Long userId);

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByIdentification(String identification);

    long countByActiveTrue();

    long countByActiveTrueAndStatusIdNot(Long statusId);

    long countByStatusIdNot(Long statusId);

    @Query("SELECT e.userId FROM Employee e WHERE e.userId IS NOT NULL")
    List<Long> findAllUserIds();

    List<Employee> findByActiveTrueAndStatusIdAndIdNotIn(Long statusId, List<Long> excludedIds);

    List<Employee> findByActiveTrueAndStatusIdAndIdIn(Long statusId, List<Long> ids);

    List<Employee> findByActiveTrueAndStatusId(Long statusId);

    List<Employee> findByUserIdIn(java.util.Collection<Long> userIds);
}
