package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.entity.Contract;
import com.crm.mcsv_rrhh.entity.ProjectAssignment;

import java.time.LocalDate;

public interface ProjectAssignmentSyncService {

    void openInitialAssignment(Contract contract);

    void syncCostCenterChange(Contract contract, Integer previousCostCenter, LocalDate effectiveDate);

    ProjectAssignment resolveAssignmentForDate(Long employeeId, LocalDate date);
}
