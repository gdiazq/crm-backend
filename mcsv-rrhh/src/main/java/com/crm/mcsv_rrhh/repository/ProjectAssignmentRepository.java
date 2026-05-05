package com.crm.mcsv_rrhh.repository;

import com.crm.mcsv_rrhh.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long>, JpaSpecificationExecutor<ProjectAssignment> {

    List<ProjectAssignment> findByEmployeeIdAndActiveTrueOrderByStartDateDesc(Long employeeId);

    List<ProjectAssignment> findByCostCenterAndActiveTrueOrderByStartDateDesc(Integer costCenter);

    long countByActiveTrue();

    @Query("""
            SELECT pa
            FROM ProjectAssignment pa
            WHERE pa.employeeId = :employeeId
              AND pa.active = true
              AND pa.startDate <= :date
              AND (pa.endDate IS NULL OR pa.endDate >= :date)
            ORDER BY pa.startDate DESC, pa.id DESC
            """)
    List<ProjectAssignment> findActiveByEmployeeAtDate(@Param("employeeId") Long employeeId,
                                                       @Param("date") LocalDate date);

    @Query("""
            SELECT pa
            FROM ProjectAssignment pa
            WHERE pa.employeeId = :employeeId
              AND pa.active = true
              AND pa.endDate IS NULL
            ORDER BY pa.startDate DESC, pa.id DESC
            """)
    List<ProjectAssignment> findOpenActiveByEmployee(@Param("employeeId") Long employeeId);

    @Query("""
            SELECT pa
            FROM ProjectAssignment pa
            WHERE pa.employeeId = :employeeId
              AND pa.costCenter = :costCenter
              AND pa.active = true
              AND (:excludeId IS NULL OR pa.id <> :excludeId)
              AND pa.startDate <= :endDate
              AND (pa.endDate IS NULL OR pa.endDate >= :startDate)
            """)
    List<ProjectAssignment> findActiveOverlaps(@Param("employeeId") Long employeeId,
                                               @Param("costCenter") Integer costCenter,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("excludeId") Long excludeId);

    Optional<ProjectAssignment> findFirstByEmployeeIdAndCostCenterAndActiveTrueAndEndDateIsNullOrderByStartDateDesc(Long employeeId,
                                                                                                                    Integer costCenter);
}
