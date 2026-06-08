package com.crm.mcsv_rrhh.mapper.leave;

import com.crm.mcsv_rrhh.dto.leave.UpdateEmployeeLeaveRequest;
import com.crm.mcsv_rrhh.entity.leave.EmployeeLeave;
import com.crm.mcsv_rrhh.util.leave.LeaveCalculator;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLeaveMapper {

    /**
     * Construye un candidato: el permiso actual con los cambios propuestos aplicados (lo no nulo del
     * proposed gana). Se usa para validar antes de persistir, sin tocar la entidad gestionada.
     */
    public EmployeeLeave mergeCandidate(EmployeeLeave current, UpdateEmployeeLeaveRequest proposed) {
        var startDate = proposed.getStartDate() != null ? proposed.getStartDate() : current.getStartDate();
        var endDate = proposed.getEndDate() != null ? proposed.getEndDate() : current.getEndDate();
        var halfDay = proposed.getHalfDay() != null ? proposed.getHalfDay() : current.getHalfDay();

        return EmployeeLeave.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .leaveTypeId(proposed.getLeaveTypeId() != null ? proposed.getLeaveTypeId() : current.getLeaveTypeId())
                .startDate(startDate)
                .endDate(endDate)
                .halfDay(Boolean.TRUE.equals(halfDay))
                .totalDays(LeaveCalculator.computeTotalDays(startDate, endDate, halfDay))
                .reason(proposed.getReason() != null ? proposed.getReason() : current.getReason())
                .createdAt(current.getCreatedAt())
                .updatedAt(current.getUpdatedAt())
                .build();
    }

    /** Copia los campos del candidato ya validado sobre el permiso gestionado. */
    public void applyChanges(EmployeeLeave target, EmployeeLeave source) {
        target.setLeaveTypeId(source.getLeaveTypeId());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setHalfDay(source.getHalfDay());
        target.setTotalDays(source.getTotalDays());
        target.setReason(source.getReason());
    }
}
