package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.CreateEmployeeRequest;
import com.crm.mcsv_rrhh.dto.EmployeeResponse;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeRequest;
import com.crm.mcsv_rrhh.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "Endpoints para la gestión de empleados (RRHH)")
public class EmployeeController {

    private final EmployeeService employeeService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "firstName", "paternalLastName", "maternalLastName",
            "identification", "corporateEmail", "active", "createdAt"
    );

    @GetMapping("/paged")
    @Operation(summary = "Listar empleados (paginado)", description = "Retorna una lista paginada de empleados con filtros opcionales")
    public ResponseEntity<PagedResponse<EmployeeResponse>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long companyId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EmployeeResponse> result = employeeService.filterEmployees(search, active, statusId, companyId, pageable);
        Map<String, Long> stats = employeeService.getEmployeeStats();

        return ResponseEntity.ok(PagedResponse.of(result, stats.get("total"), stats.get("active")));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Obtener empleado por ID")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/detail/user/{userId}")
    @Operation(summary = "Obtener empleado por userId (mcsv-user)")
    public ResponseEntity<EmployeeResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(userId));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear empleado")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar empleado")
    public ResponseEntity<EmployeeResponse> update(@Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(request.getId(), request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar empleado")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        employeeService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }
}
