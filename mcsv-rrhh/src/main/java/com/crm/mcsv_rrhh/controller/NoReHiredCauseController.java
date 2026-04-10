package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseRequest;
import com.crm.mcsv_rrhh.dto.NoReHiredCauseResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateNoReHiredCauseRequest;
import com.crm.mcsv_rrhh.service.NoReHiredCauseService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/no-re-hired-cause")
@RequiredArgsConstructor
@Tag(name = "NoReHiredCause", description = "Gestión de causas de no recontratación")
public class NoReHiredCauseController {

    private final NoReHiredCauseService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar causas de no recontratación (paginado)")
    public ResponseEntity<PagedResponse<NoReHiredCauseResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(search, active, createdFrom, createdTo, updatedFrom, updatedTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener causa de no recontratación por ID")
    public ResponseEntity<NoReHiredCauseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear causa de no recontratación")
    public ResponseEntity<NoReHiredCauseResponse> create(@Valid @RequestBody NoReHiredCauseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar causa de no recontratación")
    public ResponseEntity<NoReHiredCauseResponse> update(@Valid @RequestBody UpdateNoReHiredCauseRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar causa de no recontratación")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        service.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar causas de no recontratación a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"no-re-hired-causes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar causas de no recontratación desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.importFromCsv(file));
    }
}
