package com.crm.mcsv_rrhh.mapper.overtime;

import com.crm.mcsv_rrhh.dto.overtime.OvertimeUpdateRequest;
import com.crm.mcsv_rrhh.entity.overtime.Overtime;
import org.springframework.stereotype.Component;

@Component
public class OvertimeMapper {

    /**
     * Construye un candidato: la hora extra actual con los cambios propuestos aplicados (lo no nulo
     * del proposed gana). Se usa para validar antes de persistir, sin tocar la entidad gestionada.
     */
    public Overtime mergeCandidate(Overtime current, OvertimeUpdateRequest proposed) {
        return Overtime.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .costCenter(current.getCostCenter())
                .overtimeTypeId(proposed.getOvertimeTypeId() != null
                        ? proposed.getOvertimeTypeId() : current.getOvertimeTypeId())
                .attendanceId(current.getAttendanceId())
                .date(current.getDate())
                .startTime(proposed.getStartTime() != null
                        ? proposed.getStartTime() : current.getStartTime())
                .endTime(proposed.getEndTime() != null
                        ? proposed.getEndTime() : current.getEndTime())
                .hours(current.getHours())
                .reason(proposed.getReason() != null ? proposed.getReason() : current.getReason())
                .createdAt(current.getCreatedAt())
                .updatedAt(current.getUpdatedAt())
                .build();
    }
}
