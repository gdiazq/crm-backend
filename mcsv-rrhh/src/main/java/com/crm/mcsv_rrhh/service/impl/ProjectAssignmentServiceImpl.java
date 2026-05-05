package com.crm.mcsv_rrhh.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.client.ProjectClient;
import com.crm.mcsv_rrhh.dto.ProjectAssignmentResponse;
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
