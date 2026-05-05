package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.AttendanceRequest;
import com.crm.mcsv_rrhh.dto.AttendanceResponse;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceRequest;
import com.crm.mcsv_rrhh.entity.*;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final String ACTIVE_CONTRACT_STATUS = "Activo";
    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final AttendanceRepository repository;
    private final AttendanceStatusRepository statusRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final ContractStatusRepository contractStatusRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final EmployeeLeaveRepository employeeLeaveRepository;
    private final ProjectClient projectClient;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> list(String search,
                                                  Long employeeId,
                                                  Integer costCenter,
                                                  Long statusId,
                                                  LocalDate dateFrom,
                                                  LocalDate dateTo,
                                                  LocalDate createdFrom,
                                                  LocalDate createdTo,
                                                  LocalDate updatedFrom,
                                                  LocalDate updatedTo,
                                                  Pageable pageable,
                                                  String sortBy,
                                                  String sortDir) {
        Pageable effectivePageable = sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy)
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<Attendance> page = repository.findAll(
                AttendanceSpecification.withFilters(search, employeeId, costCenter, statusId,
                        dateFrom, dateTo, createdFrom, createdTo, updatedFrom, updatedTo, sortBy, sortDir),
                effectivePageable);

        long present = statusRepository.findByCode("PRESENT")
                .map(status -> repository.countByStatusId(status.getId()))
                .orElse(0L);
        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), present);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public AttendanceResponse create(AttendanceRequest request) {
        Employee employee = validateEmployee(request.getEmployeeId());
        if (repository.existsByEmployeeIdAndDate(employee.getId(), request.getDate())) {
            throw new DuplicateResourceException("Ya existe asistencia para el empleado en la fecha: " + request.getDate());
        }
        employeeLeaveRepository.findApprovedLeaveIdCoveringDate(employee.getId(), request.getDate())
                .ifPresent(leaveId -> {
                    throw new IllegalStateException("No se puede crear asistencia manual: existe licencia aprobada para el día. leaveId=" + leaveId);
                });

        AttendanceAssignmentSnapshot snapshot = resolveAssignmentSnapshot(
                employee, request.getProjectAssignmentId(), request.getCostCenter(), request.getDate());

        Attendance entity = Attendance.builder()
                .employeeId(employee.getId())
                .contractId(resolveActiveContractId(employee.getId()))
                .projectAssignmentId(snapshot.projectAssignmentId())
                .costCenter(snapshot.costCenter())
                .date(request.getDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .totalHours(calculateTotalHours(request.getCheckInTime(), request.getCheckOutTime()))
                .statusId(resolveActiveStatus(request.getStatusId()).getId())
                .notes(request.getNotes())
                .manuallyOverridden(false)
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public AttendanceResponse update(UpdateAttendanceRequest request) {
        Attendance entity = findOrThrow(request.getId());
        Long employeeId = request.getEmployeeId() != null ? request.getEmployeeId() : entity.getEmployeeId();
        LocalDate date = request.getDate() != null ? request.getDate() : entity.getDate();

        repository.findByEmployeeIdAndDate(employeeId, date)
                .filter(existing -> !existing.getId().equals(entity.getId()))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Ya existe asistencia para el empleado en la fecha: " + date);
                });

        Employee employee = validateEmployee(employeeId);
        Long projectAssignmentId = request.getProjectAssignmentId() != null ? request.getProjectAssignmentId() : entity.getProjectAssignmentId();
        Integer costCenter = request.getCostCenter() != null ? request.getCostCenter() : entity.getCostCenter();
        AttendanceAssignmentSnapshot snapshot = resolveAssignmentSnapshot(employee, projectAssignmentId, costCenter, date);

        LocalDateTime checkIn = request.getCheckInTime() != null ? request.getCheckInTime() : entity.getCheckInTime();
        LocalDateTime checkOut = request.getCheckOutTime() != null ? request.getCheckOutTime() : entity.getCheckOutTime();

        entity.setEmployeeId(employee.getId());
        entity.setContractId(resolveActiveContractId(employee.getId()));
        entity.setProjectAssignmentId(snapshot.projectAssignmentId());
        entity.setCostCenter(snapshot.costCenter());
        entity.setDate(date);
        entity.setCheckInTime(checkIn);
        entity.setCheckOutTime(checkOut);
        entity.setTotalHours(calculateTotalHours(checkIn, checkOut));
        if (request.getStatusId() != null) entity.setStatusId(resolveActiveStatus(request.getStatusId()).getId());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (entity.getGeneratedByLeaveId() != null) entity.setManuallyOverridden(true);

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Attendance entity = findOrThrow(id);
        repository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByEmployee(Long employeeId) {
        validateEmployee(employeeId);
        return repository.findByEmployeeIdOrderByDateDesc(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByCostCenter(Integer costCenter) {
        validateCostCenter(costCenter);
        return repository.findByCostCenterOrderByDateDesc(costCenter).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(String search,
                            Long employeeId,
                            Integer costCenter,
                            Long statusId,
                            LocalDate dateFrom,
                            LocalDate dateTo) {
        List<AttendanceResponse> rows = repository.findAll(
                        AttendanceSpecification.withFilters(search, employeeId, costCenter, statusId,
                                dateFrom, dateTo, null, null, null, null, null, null))
                .stream()
                .map(this::toResponse)
                .toList();

        StringBuilder csv = new StringBuilder("ID,RUT,Empleado,Fecha,Centro de Costo,Proyecto,Entrada,Salida,Horas,Estado Base,Licencia Activa,Notas,Fecha Creación\n");
        rows.forEach(row -> csv.append(row.getId()).append(',')
                .append(escape(row.getEmployeeIdentification())).append(',')
                .append(escape(row.getEmployeeFullName())).append(',')
                .append(row.getDate() != null ? row.getDate() : "").append(',')
                .append(row.getCostCenter() != null ? row.getCostCenter() : "").append(',')
                .append(escape(row.getProjectName())).append(',')
                .append(formatDateTime(row.getCheckInTime())).append(',')
                .append(formatDateTime(row.getCheckOutTime())).append(',')
                .append(row.getTotalHours() != null ? row.getTotalHours() : "").append(',')
                .append(escape(row.getStatusName())).append(',')
                .append(Boolean.TRUE.equals(row.getHasActiveLeave())).append(',')
                .append(escape(row.getNotes())).append(',')
                .append(formatDateTime(row.getCreatedAt())).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Attendance findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia no encontrada: " + id));
    }

    private Employee validateEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));
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

    private BigDecimal calculateTotalHours(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) return null;
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("La hora de salida debe ser posterior a la hora de entrada");
        }
        return BigDecimal.valueOf(Duration.between(checkIn, checkOut).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private AttendanceResponse toResponse(Attendance entity) {
        Employee employee = entity.getEmployee();
        AttendanceStatus status = entity.getStatus();
        return AttendanceResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .employeeFullName(employeeFullName(employee))
                .employeeIdentification(employee != null ? employee.getIdentification() : null)
                .contractId(entity.getContractId())
                .projectAssignmentId(entity.getProjectAssignmentId())
                .costCenter(entity.getCostCenter())
                .projectName(resolveProjectName(entity.getCostCenter()))
                .date(entity.getDate())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .totalHours(entity.getTotalHours())
                .statusId(entity.getStatusId())
                .statusName(status != null ? status.getName() : null)
                .statusCode(status != null ? status.getCode() : null)
                .generatedByLeaveId(entity.getGeneratedByLeaveId())
                .manuallyOverridden(entity.getManuallyOverridden())
                .hasActiveLeave(entity.getHasActiveLeave())
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

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    private record AttendanceAssignmentSnapshot(Long projectAssignmentId, Integer costCenter) {}
}
