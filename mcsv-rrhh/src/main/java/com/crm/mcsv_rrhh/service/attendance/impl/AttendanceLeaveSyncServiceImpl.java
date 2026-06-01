package com.crm.mcsv_rrhh.service.attendance.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.enums.contract.ContractStatusName;
import com.crm.mcsv_rrhh.service.attendance.AttendanceLeaveSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import com.crm.mcsv_rrhh.entity.attendance.Attendance;
import com.crm.mcsv_rrhh.entity.attendance.AttendanceStatus;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import com.crm.mcsv_rrhh.entity.contract.ContractStatus;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.entity.leave.EmployeeLeave;
import com.crm.mcsv_rrhh.entity.projectassignment.ProjectAssignment;
import com.crm.mcsv_rrhh.repository.attendance.AttendanceRepository;
import com.crm.mcsv_rrhh.repository.attendance.AttendanceStatusRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractRepository;
import com.crm.mcsv_rrhh.repository.contract.ContractStatusRepository;
import com.crm.mcsv_rrhh.repository.employee.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.projectassignment.ProjectAssignmentRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceLeaveSyncServiceImpl implements AttendanceLeaveSyncService {

    private static final String JUSTIFIED_STATUS_CODE = "JUSTIFIED";

    private final AttendanceRepository attendanceRepository;
    private final AttendanceStatusRepository attendanceStatusRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    @Override
    @Transactional
    public void generateForApprovedLeave(EmployeeLeave leave) {
        if (leave == null || leave.getId() == null || leave.getStartDate() == null || leave.getEndDate() == null) {
            return;
        }

        Employee employee = employeeRepository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + leave.getEmployeeId()));
        Long justifiedStatusId = attendanceStatusRepository.findByCode(JUSTIFIED_STATUS_CODE)
                .map(AttendanceStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de asistencia no encontrado: " + JUSTIFIED_STATUS_CODE));
        Long contractId = resolveActiveContractIdOrFallback(leave);

        LocalDate cursor = leave.getStartDate();
        while (!cursor.isAfter(leave.getEndDate())) {
            createIfAbsent(leave, employee, contractId, justifiedStatusId, cursor);
            cursor = cursor.plusDays(1);
        }
    }

    @Override
    @Transactional
    public void revertGeneratedForLeave(Long leaveId) {
        if (leaveId == null) return;
        attendanceRepository.deleteByGeneratedByLeaveIdAndManuallyOverriddenFalse(leaveId);
    }

    private void createIfAbsent(EmployeeLeave leave,
                                Employee employee,
                                Long contractId,
                                Long justifiedStatusId,
                                LocalDate date) {
        if (attendanceRepository.existsByEmployeeIdAndDate(employee.getId(), date)) {
            return;
        }

        AttendanceAssignmentSnapshot snapshot = resolveAssignmentSnapshot(employee, date);
        Attendance attendance = Attendance.builder()
                .employeeId(employee.getId())
                .contractId(contractId)
                .projectAssignmentId(snapshot.projectAssignmentId())
                .costCenter(snapshot.costCenter())
                .date(date)
                .statusId(justifiedStatusId)
                .generatedByLeaveId(leave.getId())
                .manuallyOverridden(false)
                .notes("Generado automáticamente por permiso/licencia #" + leave.getId())
                .build();

        attendanceRepository.save(attendance);
    }

    private Long resolveActiveContractIdOrFallback(EmployeeLeave leave) {
        try {
            Long activeContractStatusId = contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                    .map(ContractStatus::getId)
                    .orElse(null);
            if (activeContractStatusId == null) {
                return leave.getContractId();
            }

            List<Contract> activeContracts = contractRepository.findByEmployeeId(leave.getEmployeeId()).stream()
                    .filter(contract -> activeContractStatusId.equals(contract.getContractStatusId()))
                    .toList();
            if (activeContracts.size() == 1) {
                return activeContracts.getFirst().getId();
            }
        } catch (Exception e) {
            log.warn("No se pudo resolver contrato activo para leave {}: {}", leave.getId(), e.getMessage());
        }
        return leave.getContractId();
    }

    private AttendanceAssignmentSnapshot resolveAssignmentSnapshot(Employee employee, LocalDate date) {
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByEmployeeAtDate(employee.getId(), date);
        if (assignments.size() == 1) {
            ProjectAssignment assignment = assignments.getFirst();
            return new AttendanceAssignmentSnapshot(assignment.getId(), assignment.getCostCenter());
        }
        if (assignments.size() > 1) {
            log.warn("Empleado {} tiene múltiples asignaciones para {}. Se usa contrato activo como fallback.",
                    employee.getId(), date);
        }
        Integer fallbackCostCenter = contractStatusRepository.findByName(ContractStatusName.ACTIVE.getDisplayName())
                .flatMap(s -> contractRepository.findFirstByEmployeeIdAndContractStatusId(employee.getId(), s.getId()))
                .map(Contract::getCostCenter)
                .orElse(null);
        return new AttendanceAssignmentSnapshot(null, fallbackCostCenter);
    }

    private record AttendanceAssignmentSnapshot(Long projectAssignmentId, Integer costCenter) {}
}
