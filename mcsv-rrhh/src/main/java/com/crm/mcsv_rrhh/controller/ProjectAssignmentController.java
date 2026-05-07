package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.ProjectAssignmentResponse;
import com.crm.mcsv_rrhh.service.ProjectAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/project-assignments")
@RequiredArgsConstructor
@Tag(name = "ProjectAssignment", description = "Historial de asignaciones de empleados a centros de costo")
public class ProjectAssignmentController {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final ProjectAssignmentService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar historial de asignaciones (paginado)")
    public ResponseEntity<PagedResponse<ProjectAssignmentResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = EMPLOYEE_SORT_FIELDS.contains(sortBy)
                ? Sort.unsorted()
                : (sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.list(search, employeeId, costCenter, active,
                dateFrom, dateTo, createdFrom, createdTo, updatedFrom, updatedTo, pageable, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener asignación por ID")
    public ResponseEntity<ProjectAssignmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/select/by-employee/{employeeId}")
    @Operation(summary = "Listar historial de asignaciones por empleado")
    public ResponseEntity<List<ProjectAssignmentResponse>> selectByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.findByEmployee(employeeId));
    }

    @GetMapping("/select/by-cost-center/{costCenter}")
    @Operation(summary = "Listar asignaciones activas por centro de costo")
    public ResponseEntity<List<ProjectAssignmentResponse>> selectByCostCenter(@PathVariable Integer costCenter) {
        return ResponseEntity.ok(service.findByCostCenter(costCenter));
    }
}
