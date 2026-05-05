package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.ProjectAssignment;

import java.time.LocalDate;

public interface ProjectAssignmentSyncService {

    void openInitialAssignment(Employee employee, LocalDate startDate);

    void syncCostCenterChange(Employee employee, Integer previousCostCenter, Integer newCostCenter, LocalDate effectiveDate);

    ProjectAssignment resolveAssignmentForDate(Long employeeId, LocalDate date);
}
