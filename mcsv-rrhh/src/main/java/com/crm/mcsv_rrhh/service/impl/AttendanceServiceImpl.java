package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.AttendanceResponse;
import com.crm.mcsv_rrhh.entity.*;
import com.crm.mcsv_rrhh.repository.*;
import com.crm.mcsv_rrhh.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final AttendanceRepository repository;
    private final AttendanceStatusRepository statusRepository;
    private final EmployeeRepository employeeRepository;
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
}
