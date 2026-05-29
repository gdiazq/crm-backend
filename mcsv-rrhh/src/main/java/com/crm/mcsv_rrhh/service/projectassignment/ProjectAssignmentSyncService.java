package com.crm.mcsv_rrhh.service.projectassignment;

import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.projectassignment.ProjectAssignment;

import java.time.LocalDate;

public interface ProjectAssignmentSyncService {

    void openInitialAssignment(Contract contract);

    void syncCostCenterChange(Contract contract, Integer previousCostCenter, LocalDate effectiveDate);

    ProjectAssignment resolveAssignmentForDate(Long employeeId, LocalDate date);
}
