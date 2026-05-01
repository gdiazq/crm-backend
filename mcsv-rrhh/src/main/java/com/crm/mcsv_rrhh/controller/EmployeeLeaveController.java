package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveRequest;
import com.crm.mcsv_rrhh.dto.EmployeeLeaveResponse;
import com.crm.mcsv_rrhh.dto.UpdateEmployeeLeaveRequest;
import com.crm.mcsv_rrhh.service.EmployeeLeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
@Tag(name = "EmployeeLeave", description = "Gestión de permisos de empleados")
public class EmployeeLeaveController {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final EmployeeLeaveService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar permisos (paginado)")
    public ResponseEntity<PagedResponse<EmployeeLeaveResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String effectiveSortBy = "status".equals(sortBy) ? "currentStatusName" : sortBy;
        Sort sort = EMPLOYEE_SORT_FIELDS.contains(sortBy)
                ? Sort.unsorted()
                : (sortDir.equalsIgnoreCase("asc") ? Sort.by(effectiveSortBy).ascending() : Sort.by(effectiveSortBy).descending());
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.list(search, status, leaveTypeId, employeeId, contractId,
                startFrom, startTo, createdFrom, createdTo, updatedFrom, updatedTo, pageable, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener permiso por ID")
    public ResponseEntity<EmployeeLeaveResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear permiso de empleado (con documentos opcionales)")
    public ResponseEntity<EmployeeLeaveResponse> create(
            @RequestPart("data") @Valid EmployeeLeaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar permiso (genera HRRequest de actualización, con documentos opcionales)")
    public ResponseEntity<EmployeeLeaveResponse> update(
            @RequestPart("data") @Valid UpdateEmployeeLeaveRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(service.update(request, files));
    }

    @DeleteMapping("/{id}/documents/{fileId}")
    @Operation(summary = "Eliminar documento adjunto del permiso")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestParam("userId") Long userId) {
        service.deleteDocument(id, fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar permisos a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"leaves.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }

    @GetMapping("/select/by-employee/{employeeId}")
    @Operation(summary = "Listar permisos de un empleado (sin paginar)")
    public ResponseEntity<List<EmployeeLeaveResponse>> findByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.findByEmployee(employeeId));
    }
}
