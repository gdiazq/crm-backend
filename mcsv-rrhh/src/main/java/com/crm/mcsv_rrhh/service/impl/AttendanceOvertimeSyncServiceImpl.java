package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.entity.Attendance;
import com.crm.mcsv_rrhh.entity.Overtime;
import com.crm.mcsv_rrhh.repository.AttendanceRepository;
import com.crm.mcsv_rrhh.repository.OvertimeRepository;
import com.crm.mcsv_rrhh.service.AttendanceOvertimeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceOvertimeSyncServiceImpl implements AttendanceOvertimeSyncService {

    private final AttendanceRepository attendanceRepository;
    private final OvertimeRepository overtimeRepository;

    @Override
    @Transactional
    public void recalculateAttendanceOvertime(Long attendanceId) {
        if (attendanceId == null) return;

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asistencia no encontrada: " + attendanceId));

        BigDecimal total = overtimeRepository.findApprovedByAttendanceId(attendanceId).stream()
                .map(Overtime::getHours)
                .filter(h -> h != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            attendance.setOvertimeHours(null);
        } else {
            attendance.setOvertimeHours(total);
            attendance.setManuallyOverridden(true);
        }
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public void applyApprovedOvertime(Overtime overtime) {
        if (overtime == null) return;
        recalculateAttendanceOvertime(overtime.getAttendanceId());
    }

    @Override
    @Transactional
    public void revertOvertime(Overtime overtime) {
        if (overtime == null) return;
        recalculateAttendanceOvertime(overtime.getAttendanceId());
    }
}
