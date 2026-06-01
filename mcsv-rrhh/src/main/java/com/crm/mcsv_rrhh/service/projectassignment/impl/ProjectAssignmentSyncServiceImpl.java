package com.crm.mcsv_rrhh.service.projectassignment.impl;

import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.projectassignment.ProjectAssignment;
import com.crm.mcsv_rrhh.repository.projectassignment.ProjectAssignmentRepository;
import com.crm.mcsv_rrhh.service.projectassignment.ProjectAssignmentSyncService;
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
    public void openInitialAssignment(Contract contract) {
        if (contract == null || contract.getEmployeeId() == null || contract.getCostCenter() == null) {
            return;
        }
        validateCostCenter(contract.getCostCenter());
        if (repository.findFirstByEmployeeIdAndCostCenterAndActiveTrueAndEndDateIsNullOrderByStartDateDesc(
                contract.getEmployeeId(), contract.getCostCenter()).isPresent()) {
            return;
        }
        LocalDate startDate = contract.getStartDate() != null ? contract.getStartDate() : LocalDate.now();
        repository.save(ProjectAssignment.builder()
                .employeeId(contract.getEmployeeId())
                .costCenter(contract.getCostCenter())
                .allocationPercent(BigDecimal.valueOf(100))
                .startDate(startDate)
                .active(true)
                .build());
    }

    @Override
    @Transactional
    public void syncCostCenterChange(Contract contract, Integer previousCostCenter, LocalDate effectiveDate) {
        if (contract == null || contract.getEmployeeId() == null
                || Objects.equals(previousCostCenter, contract.getCostCenter())) {
            return;
        }

        LocalDate startDate = effectiveDate != null ? effectiveDate : LocalDate.now();
        closeOpenAssignments(contract.getEmployeeId(), startDate);

        Integer newCostCenter = contract.getCostCenter();
        if (newCostCenter == null) {
            return;
        }

        validateCostCenter(newCostCenter);
        repository.save(ProjectAssignment.builder()
                .employeeId(contract.getEmployeeId())
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
        List<ProjectAssignment> matches = repository.findByEmployeeAtDate(employeeId, date);
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
