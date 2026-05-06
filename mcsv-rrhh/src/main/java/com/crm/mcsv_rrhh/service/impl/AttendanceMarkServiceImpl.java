package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.AttendanceMarkRequest;
import com.crm.mcsv_rrhh.dto.AttendanceMarkResponse;
import com.crm.mcsv_rrhh.dto.AttendanceMarkTypeSelectItem;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceMarkRequest;
import com.crm.mcsv_rrhh.entity.*;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.AttendanceMarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceMarkServiceImpl implements AttendanceMarkService {

    private static final String CHECK_IN = "CHECK_IN";
    private static final String CHECK_OUT = "CHECK_OUT";
    private static final String ACTIVE_CONTRACT_STATUS = "Activo";
    private static final Set<String> ALLOWED_MARK_TYPES = Set.of(CHECK_IN, CHECK_OUT);

    private final AttendanceMarkRepository repository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceStatusRepository statusRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final ProjectClient projectClient;

    @Override
    @Transactional
    public AttendanceMarkResponse create(AttendanceMarkRequest request) {
        String markType = normalizeMarkType(request.getMarkType());
        LocalDate date = request.getMarkTime().toLocalDate();
        Employee employee = validateEmployee(request.getEmployeeId());

        employeeLeaveRepository.findApprovedLeaveIdCoveringDate(employee.getId(), date)
                .ifPresent(leaveId -> {
                    throw new IllegalStateException("No se puede registrar marcaje: existe licencia aprobada para el día. leaveId=" + leaveId);
                });

        Attendance attendance = resolveAttendance(request, employee, date);
        validateRequestMatchesAttendance(request, attendance);
        validateMarkOrder(attendance.getId(), markType, request.getMarkTime());

        AttendanceMark mark = AttendanceMark.builder()
                .attendanceId(attendance.getId())
                .employeeId(attendance.getEmployeeId())
                .projectAssignmentId(attendance.getProjectAssignmentId())
                .costCenter(attendance.getCostCenter())
                .date(attendance.getDate())
                .markTime(request.getMarkTime())
                .markType(markType)
                .notes(request.getNotes())
                .build();

        AttendanceMark saved = repository.save(mark);
        recalculateAttendanceSummary(attendance);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceMarkResponse update(UpdateAttendanceMarkRequest request) {
        AttendanceMark mark = repository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Marcaje de asistencia no encontrado: " + request.getId()));
        Attendance attendance = findAttendanceOrThrow(mark.getAttendanceId());

        validateImmutableUpdateRequest(request, mark, attendance);

        String markType = request.getMarkType() != null ? normalizeMarkType(request.getMarkType()) : mark.getMarkType();
        LocalDateTime markTime = request.getMarkTime() != null ? request.getMarkTime() : mark.getMarkTime();
        if (!markTime.toLocalDate().equals(attendance.getDate())) {
            throw new IllegalArgumentException("La fecha del marcaje no coincide con la asistencia");
        }
        if (repository.existsByAttendanceIdAndMarkTypeAndIdNot(attendance.getId(), markType, mark.getId())) {
            throw new DuplicateResourceException("Ya existe un marcaje " + markType + " para esta asistencia");
        }

        validateUpdatedMarkOrder(attendance.getId(), mark.getId(), markType, markTime);

        mark.setMarkType(markType);
        mark.setMarkTime(markTime);
        mark.setDate(markTime.toLocalDate());
        if (request.getNotes() != null) mark.setNotes(request.getNotes());

        AttendanceMark saved = repository.save(mark);
        recalculateAttendanceSummary(attendance);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceMarkResponse> findByAttendance(Long attendanceId) {
        findAttendanceOrThrow(attendanceId);
        return repository.findByAttendanceIdOrderByMarkTimeAsc(attendanceId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceMarkTypeSelectItem> findMarkTypes() {
        return List.of(
                new AttendanceMarkTypeSelectItem(CHECK_IN, "Entrada"),
                new AttendanceMarkTypeSelectItem(CHECK_OUT, "Salida")
        );
    }

    private Attendance resolveAttendance(AttendanceMarkRequest request, Employee employee, LocalDate date) {
        if (request.getAttendanceId() != null) {
            Attendance attendance = findAttendanceOrThrow(request.getAttendanceId());
            if (!employee.getId().equals(attendance.getEmployeeId())) {
                throw new IllegalArgumentException("El marcaje pertenece a otro empleado");
            }
            if (!date.equals(attendance.getDate())) {
                throw new IllegalArgumentException("La fecha del marcaje no coincide con la asistencia");
            }
            return attendance;
        }

        return attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date)
                .orElseGet(() -> createAttendanceForMark(request, employee, date));
    }

    private Attendance createAttendanceForMark(AttendanceMarkRequest request, Employee employee, LocalDate date) {
        AttendanceAssignmentSnapshot snapshot = resolveAssignmentSnapshot(
                employee, request.getProjectAssignmentId(), request.getCostCenter(), date);

        Attendance attendance = Attendance.builder()
                .employeeId(employee.getId())
                .contractId(resolveActiveContractId(employee.getId()))
                .projectAssignmentId(snapshot.projectAssignmentId())
                .costCenter(snapshot.costCenter())
                .date(date)
                .statusId(resolveStatusForAutoAttendance(request.getStatusId()).getId())
                .notes(request.getNotes())
                .manuallyOverridden(false)
                .build();
        return attendanceRepository.save(attendance);
    }

    private void validateRequestMatchesAttendance(AttendanceMarkRequest request, Attendance attendance) {
        if (request.getProjectAssignmentId() != null && !request.getProjectAssignmentId().equals(attendance.getProjectAssignmentId())) {
            throw new IllegalArgumentException("El projectAssignmentId del marcaje no coincide con la asistencia");
        }
        if (request.getCostCenter() != null && !request.getCostCenter().equals(attendance.getCostCenter())) {
            throw new IllegalArgumentException("El centro de costo del marcaje no coincide con la asistencia");
        }
    }

    private void validateMarkOrder(Long attendanceId, String markType, LocalDateTime markTime) {
        if (repository.existsByAttendanceIdAndMarkType(attendanceId, markType)) {
            throw new DuplicateResourceException("Ya existe un marcaje " + markType + " para esta asistencia");
        }

        if (CHECK_OUT.equals(markType)) {
            LocalDateTime checkIn = repository.findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeAsc(attendanceId, CHECK_IN)
                    .map(AttendanceMark::getMarkTime)
                    .orElseThrow(() -> new IllegalArgumentException("Debe registrar entrada antes de registrar salida"));
            if (!markTime.isAfter(checkIn)) {
                throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
            }
        }

        if (CHECK_IN.equals(markType)) {
            repository.findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeDesc(attendanceId, CHECK_OUT)
                    .map(AttendanceMark::getMarkTime)
                    .ifPresent(checkOut -> {
                        if (!checkOut.isAfter(markTime)) {
                            throw new IllegalArgumentException("La entrada debe ser anterior a la salida");
                        }
                    });
        }
    }

    private void validateImmutableUpdateRequest(UpdateAttendanceMarkRequest request, AttendanceMark mark, Attendance attendance) {
        if (request.getAttendanceId() != null && !request.getAttendanceId().equals(mark.getAttendanceId())) {
            throw new IllegalArgumentException("No se puede mover un marcaje a otra asistencia");
        }
        if (request.getEmployeeId() != null && !request.getEmployeeId().equals(mark.getEmployeeId())) {
            throw new IllegalArgumentException("No se puede mover un marcaje a otro empleado");
        }
        if (request.getProjectAssignmentId() != null && !request.getProjectAssignmentId().equals(mark.getProjectAssignmentId())) {
            throw new IllegalArgumentException("No se puede cambiar la asignación de proyecto del marcaje");
        }
        if (request.getCostCenter() != null && !request.getCostCenter().equals(mark.getCostCenter())) {
            throw new IllegalArgumentException("No se puede cambiar el centro de costo del marcaje");
        }
        if (!mark.getEmployeeId().equals(attendance.getEmployeeId())) {
            throw new IllegalArgumentException("El marcaje no coincide con el empleado de la asistencia");
        }
    }

    private void validateUpdatedMarkOrder(Long attendanceId, Long markId, String markType, LocalDateTime markTime) {
        List<AttendanceMark> otherMarks = repository.findByAttendanceIdOrderByMarkTimeAsc(attendanceId).stream()
                .filter(candidate -> !candidate.getId().equals(markId))
                .toList();

        LocalDateTime checkIn = CHECK_IN.equals(markType)
                ? markTime
                : otherMarks.stream()
                .filter(candidate -> CHECK_IN.equals(candidate.getMarkType()))
                .map(AttendanceMark::getMarkTime)
                .findFirst()
                .orElse(null);

        LocalDateTime checkOut = CHECK_OUT.equals(markType)
                ? markTime
                : otherMarks.stream()
                .filter(candidate -> CHECK_OUT.equals(candidate.getMarkType()))
                .map(AttendanceMark::getMarkTime)
                .findFirst()
                .orElse(null);

        if (CHECK_OUT.equals(markType) && checkIn == null) {
            throw new IllegalArgumentException("Debe registrar entrada antes de registrar salida");
        }
        if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
        }
    }

    private void recalculateAttendanceSummary(Attendance attendance) {
        LocalDateTime checkIn = repository.findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeAsc(attendance.getId(), CHECK_IN)
                .map(AttendanceMark::getMarkTime)
                .orElse(null);
        LocalDateTime checkOut = repository.findFirstByAttendanceIdAndMarkTypeOrderByMarkTimeDesc(attendance.getId(), CHECK_OUT)
                .map(AttendanceMark::getMarkTime)
                .orElse(null);

        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setTotalHours(calculateTotalHours(checkIn, checkOut));
        attendanceRepository.save(attendance);
    }

    private BigDecimal calculateTotalHours(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) return null;
        return BigDecimal.valueOf(Duration.between(checkIn, checkOut).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String normalizeMarkType(String markType) {
        String normalized = markType == null ? null : markType.trim().toUpperCase();
        if (!ALLOWED_MARK_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Tipo de marcaje inválido. Use CHECK_IN o CHECK_OUT");
        }
        return normalized;
    }

    private Attendance findAttendanceOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada: " + id));
    }

    private Employee validateEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));
    }

    private AttendanceStatus resolveStatusForAutoAttendance(Long statusId) {
        if (statusId != null) return resolveActiveStatus(statusId);
        return statusRepository.findByCode("PRESENT")
                .filter(status -> Boolean.TRUE.equals(status.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Estado de asistencia PRESENT no encontrado o inactivo"));
    }

    private AttendanceStatus resolveActiveStatus(Long statusId) {
        AttendanceStatus status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de asistencia no encontrado: " + statusId));
        if (!Boolean.TRUE.equals(status.getActive())) {
            throw new IllegalArgumentException("El estado de asistencia no está activo: " + statusId);
        }
        return status;
    }

    private Long resolveActiveContractId(Long employeeId) {
        Long activeContractStatusId = contractStatusRepository.findByName(ACTIVE_CONTRACT_STATUS)
                .map(ContractStatus::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de contrato no encontrado: " + ACTIVE_CONTRACT_STATUS));

        List<Contract> activeContracts = contractRepository.findByEmployeeId(employeeId).stream()
                .filter(contract -> activeContractStatusId.equals(contract.getContractStatusId()))
                .toList();

        if (activeContracts.isEmpty()) {
            throw new IllegalArgumentException("El empleado no tiene un contrato activo");
        }
        if (activeContracts.size() > 1) {
            throw new IllegalArgumentException("El empleado tiene más de un contrato activo");
        }
        return activeContracts.getFirst().getId();
    }

    private AttendanceAssignmentSnapshot resolveAssignmentSnapshot(Employee employee,
                                                                  Long projectAssignmentId,
                                                                  Integer requestedCostCenter,
                                                                  LocalDate date) {
        if (projectAssignmentId != null) {
            ProjectAssignment assignment = projectAssignmentRepository.findById(projectAssignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Asignación de proyecto no encontrada: " + projectAssignmentId));
            validateAssignmentForDate(assignment, employee.getId(), date);
            if (requestedCostCenter != null && !requestedCostCenter.equals(assignment.getCostCenter())) {
                throw new IllegalArgumentException("El centro de costo no coincide con la asignación indicada");
            }
            return new AttendanceAssignmentSnapshot(assignment.getId(), assignment.getCostCenter());
        }

        List<ProjectAssignment> assignments = projectAssignmentRepository.findByEmployeeAtDate(employee.getId(), date);
        if (assignments.size() == 1) {
            ProjectAssignment assignment = assignments.getFirst();
            if (requestedCostCenter != null && !requestedCostCenter.equals(assignment.getCostCenter())) {
                throw new IllegalArgumentException("El centro de costo no coincide con la asignación vigente para la fecha");
            }
            return new AttendanceAssignmentSnapshot(assignment.getId(), assignment.getCostCenter());
        }
        if (assignments.size() > 1) {
            String candidates = assignments.stream()
                    .map(a -> "id=" + a.getId() + ", costCenter=" + a.getCostCenter())
                    .toList()
                    .toString();
            throw new IllegalArgumentException("El empleado tiene varias asignaciones para la fecha. Debe indicar projectAssignmentId. Candidatas: " + candidates);
        }

        Integer fallbackCostCenter = requestedCostCenter != null ? requestedCostCenter : employee.getCostCenter();
        if (fallbackCostCenter != null) validateCostCenter(fallbackCostCenter);
        return new AttendanceAssignmentSnapshot(null, fallbackCostCenter);
    }

    private void validateAssignmentForDate(ProjectAssignment assignment, Long employeeId, LocalDate date) {
        if (!employeeId.equals(assignment.getEmployeeId())) {
            throw new IllegalArgumentException("La asignación indicada pertenece a otro empleado");
        }
        if (assignment.getStartDate() != null && assignment.getStartDate().isAfter(date)) {
            throw new IllegalArgumentException("La asignación indicada no está vigente en la fecha");
        }
        if (assignment.getEndDate() != null && assignment.getEndDate().isBefore(date)) {
            throw new IllegalArgumentException("La asignación indicada no está vigente en la fecha");
        }
    }

    private void validateCostCenter(Integer costCenter) {
        if (costCenter == null || costCenter <= 0) {
            throw new IllegalArgumentException("El centro de costo debe ser mayor a 0");
        }
        try {
            ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
            if (project == null || project.getId() == null) {
                throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
        }
    }

    private AttendanceMarkResponse toResponse(AttendanceMark entity) {
        Employee employee = entity.getEmployee() != null
                ? entity.getEmployee()
                : employeeRepository.findById(entity.getEmployeeId()).orElse(null);
        return AttendanceMarkResponse.builder()
                .id(entity.getId())
                .attendanceId(entity.getAttendanceId())
                .employeeId(entity.getEmployeeId())
                .employeeFullName(employeeFullName(employee))
                .employeeIdentification(employee != null ? employee.getIdentification() : null)
                .projectAssignmentId(entity.getProjectAssignmentId())
                .costCenter(entity.getCostCenter())
                .projectName(resolveProjectName(entity.getCostCenter()))
                .date(entity.getDate())
                .markTime(entity.getMarkTime())
                .markType(entity.getMarkType())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String resolveProjectName(Integer costCenter) {
        if (costCenter == null) return null;
        try {
            ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(costCenter);
            return project != null ? project.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String employeeFullName(Employee employee) {
        if (employee == null) return null;
        return String.join(" ", safe(employee.getFirstName()), safe(employee.getPaternalLastName()), safe(employee.getMaternalLastName())).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record AttendanceAssignmentSnapshot(Long projectAssignmentId, Integer costCenter) {}
}
