package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_rrhh.dto.LegalTerminationCauseRequest;
import com.crm.mcsv_rrhh.dto.LegalTerminationCauseResponse;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.UpdateLegalTerminationCauseRequest;
import com.crm.mcsv_rrhh.service.LegalTerminationCauseService;
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
@RequestMapping("/legal-termination-cause")
@RequiredArgsConstructor
@Tag(name = "LegalTerminationCause", description = "Gestión de causales legales de término")
public class LegalTerminationCauseController {

    private final LegalTerminationCauseService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar causales legales (paginado)")
    public ResponseEntity<PagedResponse<LegalTerminationCauseResponse>> paged(
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
    @Operation(summary = "Obtener causal legal por ID")
    public ResponseEntity<LegalTerminationCauseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear causal legal")
    public ResponseEntity<LegalTerminationCauseResponse> create(@Valid @RequestBody LegalTerminationCauseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar causal legal")
    public ResponseEntity<LegalTerminationCauseResponse> update(@Valid @RequestBody UpdateLegalTerminationCauseRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar causal legal")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        service.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar causales legales a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"legal-termination-causes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar causales legales desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.importFromCsv(file));
    }
}
