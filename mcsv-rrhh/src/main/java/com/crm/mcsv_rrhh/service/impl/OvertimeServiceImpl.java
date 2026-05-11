package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.OvertimeRequest;
import com.crm.mcsv_rrhh.dto.OvertimeResponse;
import com.crm.mcsv_rrhh.dto.OvertimeUpdateRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.HRRequest;
import com.crm.mcsv_rrhh.entity.Overtime;
import com.crm.mcsv_rrhh.entity.OvertimeType;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.EmployeeStatusRepository;
import com.crm.mcsv_rrhh.repository.HRRequestRepository;
import com.crm.mcsv_rrhh.repository.OvertimeRepository;
import com.crm.mcsv_rrhh.repository.OvertimeSpecification;
import com.crm.mcsv_rrhh.repository.OvertimeTypeRepository;
import com.crm.mcsv_rrhh.service.HRRequestService;
import com.crm.mcsv_rrhh.service.OvertimeService;
import com.crm.mcsv_rrhh.util.OvertimeValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeRepository overtimeRepository;
    private final OvertimeTypeRepository overtimeTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final HRRequestRepository hrRequestRepository;
    private final HRRequestService hrRequestService;
    private final OvertimeValidator overtimeValidator;
    private final ProjectClient projectClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OvertimeResponse> list(Long employeeId, Integer costCenter, Long statusId,
                                                LocalDate dateFrom, LocalDate dateTo,
                                                Long overtimeTypeId, Pageable pageable) {
        Page<Overtime> page = overtimeRepository.findAll(
                OvertimeSpecification.withFilters(employeeId, costCenter, statusId, dateFrom, dateTo, overtimeTypeId),
                pageable);

        long total = page.getTotalElements();
        long active = resolveCountByStatusName("Aprobado");
        long pending = resolveCountByStatusNames(List.of("Pendiente de revisión", "Pendiente de aprobación"));

        return PagedResponse.of(page.map(this::toResponse), total, active, pending);
    }

    @Override
    @Transactional(readOnly = true)
    public OvertimeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public OvertimeResponse create(OvertimeRequest request, Long userId) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empleado no encontrado: " + request.getEmployeeId()));

        Overtime candidate = Overtime.builder()
                .employeeId(employee.getId())
                .contractId(null)
                .costCenter(employee.getCostCenter())
                .overtimeTypeId(request.getOvertimeTypeId())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .build();

        BigDecimal hours = overtimeValidator.validate(candidate, null);
        candidate.setHours(hours);

        Overtime saved = overtimeRepository.save(candidate);
        hrRequestService.createForOvertime(saved.getId(), saved.getEmployeeId(), "CREATE", null);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public OvertimeResponse update(OvertimeUpdateRequest request, Long userId) {
        Overtime current = findOrThrow(request.getId());

        Overtime candidate = Overtime.builder()
                .id(current.getId())
                .employeeId(current.getEmployeeId())
                .contractId(current.getContractId())
                .costCenter(current.getCostCenter())
                .overtimeTypeId(request.getOvertimeTypeId() != null
                        ? request.getOvertimeTypeId() : current.getOvertimeTypeId())
                .attendanceId(current.getAttendanceId())
                .date(current.getDate())
                .startTime(request.getStartTime() != null ? request.getStartTime() : current.getStartTime())
                .endTime(request.getEndTime() != null ? request.getEndTime() : current.getEndTime())
                .reason(request.getReason() != null ? request.getReason() : current.getReason())
                .build();

        overtimeValidator.validate(candidate, current.getId());

        String proposedData;
        try {
            proposedData = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando proposedData", e);
        }

        hrRequestService.createForOvertime(current.getId(), current.getEmployeeId(), "UPDATE", proposedData);

        return toResponse(current);
    }

    private long resolveCountByStatusName(String statusName) {
        return employeeStatusRepository.findByName(statusName)
                .map(s -> hrRequestRepository.countOvertimesWithLatestStatusId(s.getId()))
                .orElse(0L);
    }

    private long resolveCountByStatusNames(List<String> names) {
        List<Long> ids = employeeStatusRepository.findAllByNameIn(names).stream()
                .map(s -> s.getId()).toList();
        if (ids.isEmpty()) return 0L;
        return hrRequestRepository.countOvertimesWithLatestStatusIdIn(ids);
    }

    private Overtime findOrThrow(Long id) {
        return overtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hora extra no encontrada: " + id));
    }

    private OvertimeResponse toResponse(Overtime ot) {
        Employee employee = ot.getEmployee();
        OvertimeType type = ot.getOvertimeType();

        Optional<HRRequest> latestHr = hrRequestRepository.findTopByOvertimeIdOrderByCreatedAtDesc(ot.getId());
        String statusName = latestHr.map(hr -> employeeStatusRepository.findById(hr.getStatusId())
                        .map(s -> s.getName()).orElse(null))
                .orElse(ot.getCurrentStatusName());

        String projectName = null;
        if (ot.getCostCenter() != null) {
            try {
                ProjectClient.ProjectNameDTO project = projectClient.getByCostCenter(ot.getCostCenter());
                if (project != null) projectName = project.getName();
            } catch (Exception e) {
                log.warn("No se pudo resolver proyecto del costCenter {}: {}", ot.getCostCenter(), e.getMessage());
            }
        }

        return OvertimeResponse.builder()
                .id(ot.getId())
                .employeeId(ot.getEmployeeId())
                .employeeName(fullName(employee))
                .contractId(ot.getContractId())
                .costCenter(ot.getCostCenter())
                .projectName(projectName)
                .overtimeTypeId(ot.getOvertimeTypeId())
                .overtimeTypeName(type != null ? type.getName() : null)
                .surchargePercent(type != null ? type.getSurchargePercent() : null)
                .attendanceId(ot.getAttendanceId())
                .date(ot.getDate())
                .startTime(ot.getStartTime())
                .endTime(ot.getEndTime())
                .hours(ot.getHours())
                .reason(ot.getReason())
                .currentStatusName(statusName)
                .createdAt(ot.getCreatedAt())
                .updatedAt(ot.getUpdatedAt())
                .build();
    }

    private String fullName(Employee employee) {
        if (employee == null) return null;
        return String.join(" ", employee.getFirstName(),
                employee.getPaternalLastName() != null ? employee.getPaternalLastName() : "",
                employee.getMaternalLastName() != null ? employee.getMaternalLastName() : "").trim();
    }
}
