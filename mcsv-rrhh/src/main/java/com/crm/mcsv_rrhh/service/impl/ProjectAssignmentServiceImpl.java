package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.ProjectAssignmentRequest;
import com.crm.mcsv_rrhh.dto.ProjectAssignmentResponse;
import com.crm.mcsv_rrhh.dto.UpdateProjectAssignmentRequest;
import com.crm.mcsv_rrhh.entity.Employee;
import com.crm.mcsv_rrhh.entity.ProjectAssignment;
import com.crm.mcsv_rrhh.repository.EmployeeRepository;
import com.crm.mcsv_rrhh.repository.ProjectAssignmentRepository;
import com.crm.mcsv_rrhh.repository.ProjectAssignmentSpecification;
import com.crm.mcsv_rrhh.service.ProjectAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProjectAssignmentServiceImpl implements ProjectAssignmentService {

    private static final LocalDate OPEN_END_DATE = LocalDate.of(9999, 12, 31);
    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final ProjectAssignmentRepository repository;
    private final EmployeeRepository employeeRepository;
    private final ProjectClient projectClient;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProjectAssignmentResponse> list(String search,
                                                         Long employeeId,
                                                         Integer costCenter,
                                                         Boolean active,
                                                         LocalDate dateFrom,
                                                         LocalDate dateTo,
                                                         LocalDate createdFrom,
                                                         LocalDate createdTo,
                                                         LocalDate updatedFrom,
                                                         LocalDate updatedTo,
                                                         Pageable pageable,
                                                         String sortBy,
                                                         String sortDir) {
        Pageable effectivePageable = (sortBy != null && EMPLOYEE_SORT_FIELDS.contains(sortBy))
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        Page<ProjectAssignment> page = repository.findAll(
                ProjectAssignmentSpecification.withFilters(search, employeeId, costCenter, active,
                        dateFrom, dateTo, createdFrom, createdTo, updatedFrom, updatedTo, sortBy, sortDir),
                effectivePageable);

        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), repository.countByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAssignmentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ProjectAssignmentResponse create(ProjectAssignmentRequest request) {
        Employee employee = validateEmployee(request.getEmployeeId());
        validateCostCenter(request.getCostCenter());
        validateDates(request.getStartDate(), request.getEndDate());
        validateManualCreatePolicy(request);
        validateNoActiveOverlap(employee.getId(), request.getCostCenter(), request.getStartDate(), request.getEndDate(), null);

        ProjectAssignment entity = ProjectAssignment.builder()
                .employeeId(employee.getId())
                .costCenter(request.getCostCenter())
                .roleOnProject(request.getRoleOnProject())
                .allocationPercent(request.getAllocationPercent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public ProjectAssignmentResponse update(UpdateProjectAssignmentRequest request) {
        ProjectAssignment entity = findOrThrow(request.getId());

        Long employeeId = request.getEmployeeId() != null ? request.getEmployeeId() : entity.getEmployeeId();
        Integer costCenter = request.getCostCenter() != null ? request.getCostCenter() : entity.getCostCenter();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : entity.getEndDate();
        Boolean active = request.getActive() != null ? request.getActive() : entity.getActive();

        validateEmployee(employeeId);
        validateCostCenter(costCenter);
        validateDates(startDate, endDate);
        if (Boolean.TRUE.equals(active)) {
            validateNoActiveOverlap(employeeId, costCenter, startDate, endDate, entity.getId());
        }

        entity.setEmployeeId(employeeId);
        entity.setCostCenter(costCenter);
        entity.setStartDate(startDate);
        entity.setEndDate(endDate);
        entity.setActive(active);

        if (request.getRoleOnProject() != null) {
            entity.setRoleOnProject(request.getRoleOnProject());
        }
        if (request.getAllocationPercent() != null) {
            entity.setAllocationPercent(request.getAllocationPercent());
        }

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        ProjectAssignment entity = findOrThrow(id);
        entity.setActive(false);
        if (entity.getEndDate() == null) {
            entity.setEndDate(LocalDate.now());
        }
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignmentResponse> findByEmployee(Long employeeId) {
        validateEmployee(employeeId);
        return repository.findByEmployeeIdAndActiveTrueOrderByStartDateDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAssignmentResponse> findByCostCenter(Integer costCenter) {
        validateCostCenter(costCenter);
        return repository.findByCostCenterAndActiveTrueOrderByStartDateDesc(costCenter)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ProjectAssignment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de proyecto no encontrada: " + id));
    }

    private Employee validateEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + employeeId));
    }

    private void validateCostCenter(Integer costCenter) {
        if (costCenter == null || costCenter <= 0) {
            throw new IllegalArgumentException("El centro de costo es obligatorio y debe ser mayor a 0");
        }
        ProjectClient.ProjectNameDTO project;
        try {
            project = projectClient.getByCostCenter(costCenter);
        } catch (Exception e) {
            throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
        }
        if (project == null || project.getId() == null) {
            throw new IllegalArgumentException("Centro de costo inválido o servicio de proyectos no disponible: " + costCenter);
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio");
        }
    }

    private void validateManualCreatePolicy(ProjectAssignmentRequest request) {
        boolean isCurrentOrFuture = request.getEndDate() == null || !request.getEndDate().isBefore(LocalDate.now());
        if (isCurrentOrFuture && !Boolean.TRUE.equals(request.getRegularization())) {
            throw new IllegalStateException("La creación manual de asignaciones vigentes requiere regularization=true");
        }
    }

    private void validateNoActiveOverlap(Long employeeId, Integer costCenter, LocalDate startDate, LocalDate endDate, Long excludeId) {
        LocalDate effectiveEndDate = endDate != null ? endDate : OPEN_END_DATE;
        List<ProjectAssignment> overlaps = repository.findActiveOverlaps(employeeId, costCenter, startDate, effectiveEndDate, excludeId);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Ya existe una asignación activa solapada para este empleado y centro de costo");
        }
    }

    private ProjectAssignmentResponse toResponse(ProjectAssignment entity) {
        Employee employee = entity.getEmployee();
        return ProjectAssignmentResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .employeeFullName(employeeFullName(employee))
                .employeeIdentification(employee != null ? employee.getIdentification() : null)
                .costCenter(entity.getCostCenter())
                .projectName(resolveProjectName(entity.getCostCenter()))
                .roleOnProject(entity.getRoleOnProject())
                .allocationPercent(entity.getAllocationPercent())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .active(entity.getActive())
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
        return String.join(" ",
                safe(employee.getFirstName()),
                safe(employee.getPaternalLastName()),
                safe(employee.getMaternalLastName())).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
