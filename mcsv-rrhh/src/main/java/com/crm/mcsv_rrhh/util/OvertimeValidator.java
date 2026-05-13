package com.crm.mcsv_rrhh.util;

import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.entity.Attendance;
import com.crm.mcsv_rrhh.entity.AttendanceMark;
import com.crm.mcsv_rrhh.entity.AttendanceMarkType;
import com.crm.mcsv_rrhh.entity.Overtime;
import com.crm.mcsv_rrhh.entity.OvertimeType;
import com.crm.mcsv_rrhh.entity.RequestStatus;
import com.crm.mcsv_rrhh.repository.AttendanceMarkRepository;
import com.crm.mcsv_rrhh.repository.AttendanceRepository;
import com.crm.mcsv_rrhh.repository.EmployeeLeaveRepository;
import com.crm.mcsv_rrhh.repository.OvertimeRepository;
import com.crm.mcsv_rrhh.repository.OvertimeTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OvertimeValidator {

    @Value("${overtime.max-hours-per-block:12.00}")
    private BigDecimal maxHoursPerBlock;

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMarkRepository attendanceMarkRepository;
    private final OvertimeRepository overtimeRepository;
    private final OvertimeTypeRepository overtimeTypeRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final ProjectClient projectClient;

    public record Result(Long attendanceId, BigDecimal hours) {}

    public Result validate(Overtime candidate, Long excludeOvertimeId) {
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(candidate.getEmployeeId(), candidate.getDate())
                .orElseThrow(() -> new IllegalArgumentException("Debe existir asistencia del día"));

        validateTimeRange(candidate.getStartTime(), candidate.getEndTime(), candidate.getDate());
        validateOutsideAttendanceMarks(candidate.getStartTime(), candidate.getEndTime(), attendance.getId());

        BigDecimal hours = computeHours(candidate.getStartTime(), candidate.getEndTime());
        if (hours.compareTo(maxHoursPerBlock) > 0) {
            throw new IllegalArgumentException(
                    "Un bloque de horas extras no puede superar las " + maxHoursPerBlock + " horas");
        }

        validateNoOverlapWithActiveBlocks(candidate, excludeOvertimeId);
        validateOvertimeType(candidate.getOvertimeTypeId());
        validateProject(candidate.getCostCenter());
        validateNoApprovedLeave(candidate.getEmployeeId(), candidate.getDate());

        return new Result(attendance.getId(), hours);
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end, LocalDate date) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de término");
        }
        if (!start.toLocalDate().equals(date) || !end.toLocalDate().equals(date)) {
            throw new IllegalArgumentException(
                    "Las horas de inicio y término deben pertenecer a la fecha del registro");
        }
    }

    private void validateOutsideAttendanceMarks(LocalDateTime start, LocalDateTime end, Long attendanceId) {
        LocalDateTime checkIn = attendanceMarkRepository
                .findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeAsc(attendanceId, AttendanceMarkType.CHECK_IN.name())
                .map(AttendanceMark::getMarkTime)
                .orElse(null);
        LocalDateTime checkOut = attendanceMarkRepository
                .findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeDesc(attendanceId, AttendanceMarkType.CHECK_OUT.name())
                .map(AttendanceMark::getMarkTime)
                .orElse(null);

        if (checkIn == null && checkOut == null) {
            throw new IllegalArgumentException(
                    "Debe existir un marcaje de entrada o salida para registrar horas extras");
        }

        boolean beforeEntry = checkIn != null && !end.isAfter(checkIn);
        boolean afterExit = checkOut != null && !start.isBefore(checkOut);
        if (beforeEntry || afterExit) return;

        throw new IllegalArgumentException(
                "El bloque debe estar completamente antes del marcaje de entrada o después del marcaje de salida");
    }

    private BigDecimal computeHours(LocalDateTime start, LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes <= 0) {
            throw new IllegalArgumentException("La duración del bloque debe ser mayor a 0");
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private void validateNoOverlapWithActiveBlocks(Overtime candidate, Long excludeOvertimeId) {
        List<Overtime> sameDay = overtimeRepository
                .findByEmployeeIdAndDate(candidate.getEmployeeId(), candidate.getDate());
        for (Overtime other : sameDay) {
            if (excludeOvertimeId != null && excludeOvertimeId.equals(other.getId())) continue;
            String status = other.getCurrentStatusName();
            if (status == null || !RequestStatus.ACTIVE_DISPLAY_NAMES.contains(status)) continue;
            if (overlaps(candidate.getStartTime(), candidate.getEndTime(),
                         other.getStartTime(), other.getEndTime())) {
                throw new IllegalArgumentException(
                        "El bloque se solapa con otro registro activo del mismo día");
            }
        }
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd,
                             LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private void validateOvertimeType(Long overtimeTypeId) {
        OvertimeType type = overtimeTypeRepository.findById(overtimeTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de hora extra no encontrado: " + overtimeTypeId));
        if (!Boolean.TRUE.equals(type.getActive())) {
            throw new IllegalArgumentException("El tipo de hora extra está inactivo");
        }
    }

    private void validateProject(Integer costCenter) {
        if (costCenter == null) {
            throw new IllegalArgumentException("El empleado no tiene centro de costo asignado");
        }
        ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException(
                    "El centro de costo del empleado no corresponde a un proyecto válido");
        }
    }

    private void validateNoApprovedLeave(Long employeeId, LocalDate date) {
        employeeLeaveRepository
                .findApprovedLeaveIdCoveringDate(employeeId, date)
                .ifPresent(id -> {
                    throw new IllegalArgumentException("El empleado tiene un permiso aprobado ese día");
                });
    }
}
