package com.crm.mcsv_rrhh.service.impl;

import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.ProjectAssignment;
import com.crm.mcsv_rrhh.repository.ProjectAssignmentRepository;
import com.crm.mcsv_rrhh.service.ProjectAssignmentSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectAssignmentSyncServiceImpl implements ProjectAssignmentSyncService {

    private final ProjectAssignmentRepository repository;
    private final ProjectClient projectClient;

    @Override
    @Transactional
    public void openInitialAssignment(Employee employee, LocalDate startDate) {
        if (employee == null || employee.getId() == null || employee.getCostCenter() == null) {
            return;
        }
        validateCostCenter(employee.getCostCenter());
        if (repository.findFirstByEmployeeIdAndCostCenterAndActiveTrueAndEndDateIsNullOrderByStartDateDesc(
                employee.getId(), employee.getCostCenter()).isPresent()) {
            return;
        }
        repository.save(ProjectAssignment.builder()
                .employeeId(employee.getId())
                .costCenter(employee.getCostCenter())
                .allocationPercent(BigDecimal.valueOf(100))
                .startDate(startDate != null ? startDate : LocalDate.now())
                .active(true)
                .build());
    }

    @Override
    @Transactional
    public void syncCostCenterChange(Employee employee, Integer previousCostCenter, Integer newCostCenter, LocalDate effectiveDate) {
        if (employee == null || employee.getId() == null || Objects.equals(previousCostCenter, newCostCenter)) {
            return;
        }

        LocalDate startDate = effectiveDate != null ? effectiveDate : LocalDate.now();
        closeOpenAssignments(employee.getId(), startDate);

        if (newCostCenter == null) {
            return;
        }

        validateCostCenter(newCostCenter);
        repository.save(ProjectAssignment.builder()
                .employeeId(employee.getId())
                .costCenter(newCostCenter)
                .allocationPercent(BigDecimal.valueOf(100))
                .startDate(startDate)
                .active(true)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAssignment resolveAssignmentForDate(Long employeeId, LocalDate date) {
        if (employeeId == null || date == null) {
            return null;
        }
        List<ProjectAssignment> matches = repository.findActiveByEmployeeAtDate(employeeId, date);
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private void closeOpenAssignments(Long employeeId, LocalDate newStartDate) {
        LocalDate closeDate = newStartDate.minusDays(1);
        List<ProjectAssignment> openAssignments = repository.findOpenActiveByEmployee(employeeId);
        for (ProjectAssignment assignment : openAssignments) {
            LocalDate effectiveCloseDate = closeDate.isBefore(assignment.getStartDate())
                    ? assignment.getStartDate()
                    : closeDate;
            assignment.setEndDate(effectiveCloseDate);
            assignment.setActive(false);
        }
        repository.saveAll(openAssignments);
    }

    private void validateCostCenter(Integer costCenter) {
        if (costCenter == null || costCenter <= 0) {
            throw new IllegalArgumentException("El centro de costo es obligatorio y debe ser mayor a 0");
        }
        ProjectClient.ProjectNameDTO project;
        try {
            project = projectClient.getByCostCenter(costCenter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
        }
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
        }
    }
}
