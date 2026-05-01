package com.crm.mcsv_rrhh.util;

import com.crm.common.service.StorageService;
import com.crm.mcsv_rrhh.entity.EmployeeLeave;
import com.crm.mcsv_rrhh.entity.LeaveType;
import com.crm.mcsv_rrhh.repository.EmployeeLeaveRepository;
import com.crm.mcsv_rrhh.repository.EmployeeLeaveSpecification;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveValidator {

    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final StorageService storageService;

    public void validate(EmployeeLeave candidate, List<MultipartFile> files, Long excludeLeaveId, Long pendingHrRequestId) {
        validateDateRange(candidate.getStartDate(), candidate.getEndDate(), candidate.getHalfDay());

        LeaveType leaveType = leaveTypeRepository.findById(candidate.getLeaveTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de permiso no encontrado: " + candidate.getLeaveTypeId()));

        validateDocumentRequirement(leaveType, files, excludeLeaveId, pendingHrRequestId);
        validateOverlap(candidate.getEmployeeId(), candidate.getStartDate(), candidate.getEndDate(), excludeLeaveId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, Boolean halfDay) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de término");
        }
        if (Boolean.TRUE.equals(halfDay) && !startDate.equals(endDate)) {
            throw new IllegalArgumentException("Un permiso de medio día debe tener la misma fecha de inicio y término");
        }
    }

    private void validateDocumentRequirement(LeaveType leaveType, List<MultipartFile> files, Long existingLeaveId, Long pendingHrRequestId) {
        if (!Boolean.TRUE.equals(leaveType.getRequiresDocument())) {
            return;
        }

        boolean hasNewFiles = files != null && !files.isEmpty();
        boolean hasExistingFiles = existingLeaveId != null
                && !storageService.listByEntity("LEAVE", existingLeaveId).isEmpty();
        boolean hasPendingFiles = pendingHrRequestId != null
                && !storageService.listByEntity("LEAVE_PENDING", pendingHrRequestId).isEmpty();

        if (!hasNewFiles && !hasExistingFiles && !hasPendingFiles) {
            throw new IllegalArgumentException("El tipo de permiso requiere al menos un documento adjunto");
        }
    }

    private void validateOverlap(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeLeaveId) {
        List<Long> activeStatusIds = employeeStatusRepository
                .findAllByNameIn(List.of("Pendiente de revisión", "Pendiente de aprobación", "Aprobado"))
                .stream()
                .map(s -> s.getId())
                .toList();

        boolean hasOverlap = !employeeLeaveRepository.findAll(
                EmployeeLeaveSpecification.overlapsForEmployee(employeeId, startDate, endDate, excludeLeaveId, activeStatusIds)
        ).isEmpty();

        if (hasOverlap) {
            throw new IllegalStateException("Existe un permiso activo o pendiente que se cruza con el rango indicado");
        }
    }
}
