package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.OvertimeRequest;
import com.crm.mcsv_rrhh.dto.OvertimeResponse;
import com.crm.mcsv_rrhh.dto.OvertimeTypeResponse;
import com.crm.mcsv_rrhh.dto.OvertimeUpdateRequest;
import com.crm.mcsv_rrhh.service.OvertimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/overtime")
@RequiredArgsConstructor
@Tag(name = "Overtime", description = "Gestión de horas extras")
public class OvertimeController {

    private final OvertimeService service;

    @GetMapping
    @Operation(summary = "Listar horas extras (paginado)")
    public ResponseEntity<PagedResponse<OvertimeResponse>> list(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long overtimeTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.list(employeeId, costCenter, statusId,
                dateFrom, dateTo, overtimeTypeId, pageable));
    }

    @GetMapping("/types")
    @Operation(summary = "Listar tipos de hora extra activos")
    public ResponseEntity<List<OvertimeTypeResponse>> listTypes() {
        return ResponseEntity.ok(service.listTypes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener hora extra por ID")
    public ResponseEntity<OvertimeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear hora extra")
    public ResponseEntity<OvertimeResponse> create(
            @RequestBody @Valid OvertimeRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar hora extra (genera HRRequest de actualización)")
    public ResponseEntity<OvertimeResponse> update(
            @RequestBody @Valid OvertimeUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(service.update(request, userId));
    }
}
