package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.*;
import com.crm.mcsv_rrhh.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate createdFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate createdTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EmployeeResponse> result = employeeService.filterEmployees(search, active, statusId, createdFrom, createdTo, pageable);
        Map<String, Long> stats = employeeService.getEmployeeStats();

        return ResponseEntity.ok(PagedResponse.of(result, stats.get("total"), stats.get("active")));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Obtener empleado por ID")
    public ResponseEntity<EmployeeDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/detail/user/{userId}")
    @Operation(summary = "Obtener empleado por userId (mcsv-user)")
    public ResponseEntity<EmployeeDetailResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(userId));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear empleado")
    public ResponseEntity<EmployeeDetailResponse> create(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar empleado")
    public ResponseEntity<EmployeeDetailResponse> update(@Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(request.getId(), request));
    }

    @GetMapping("/select/supervisors")
    @Operation(summary = "Empleados supervisores", description = "Retorna empleados cuyo usuario vinculado tiene un rol de supervisor")
    public ResponseEntity<List<EmployeeService.EmployeeSelectItem>> getSupervisors() {
        return ResponseEntity.ok(employeeService.getSupervisors());
    }

    @GetMapping("/select/visitors")
    @Operation(summary = "Empleados visitadores", description = "Retorna empleados cuyo usuario vinculado tiene un rol de visitador")
    public ResponseEntity<List<EmployeeService.EmployeeSelectItem>> getVisitors() {
        return ResponseEntity.ok(employeeService.getVisitors());
    }

    @GetMapping("/select/without-contract")
    @Operation(summary = "Empleados sin contrato", description = "Retorna empleados activos y aprobados que aún no tienen contrato")
    public ResponseEntity<List<EmployeeService.EmployeeSelectItem>> getEmployeesWithoutContract() {
        return ResponseEntity.ok(employeeService.getEmployeesWithoutContract());
    }

    @GetMapping("/select/with-contract")
    @Operation(summary = "Empleados con contrato", description = "Retorna empleados activos y aprobados que tienen contrato")
    public ResponseEntity<List<EmployeeService.EmployeeSelectItem>> getEmployeesWithContract() {
        return ResponseEntity.ok(employeeService.getEmployeesWithContract());
    }

    @GetMapping("/select/company-representatives")
    @Operation(summary = "Representantes de empresa", description = "Retorna empleados cuyo usuario vinculado tiene un rol de representante de empresa")
    public ResponseEntity<List<EmployeeService.EmployeeSelectItem>> getCompanyRepresentatives() {
        return ResponseEntity.ok(employeeService.getCompanyRepresentatives());
    }

    @GetMapping("/select/available-users")
    @Operation(summary = "Usuarios disponibles para vincular a empleado", description = "Retorna usuarios excluyendo los ya vinculados a un empleado")
    public ResponseEntity<List<CatalogItem>> getAvailableUsers(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(employeeService.getAvailableUsersForEmployee(search));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activar o desactivar empleado")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        employeeService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/link-user")
    @Operation(summary = "Vincular/desvincular usuario del sistema a un empleado")
    public ResponseEntity<Void> updateLinkedUser(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        employeeService.updateLinkedUser(id, body.get("userId"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar empleados a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(employeeService.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar empleados desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.importFromCsv(file));
    }
}
