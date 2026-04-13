package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.SettlementRequest;
import com.crm.mcsv_rrhh.dto.SettlementResponse;
import com.crm.mcsv_rrhh.dto.UpdateSettlementRequest;
import com.crm.mcsv_rrhh.service.SettlementService;
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
import java.util.List;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement", description = "Gestión de finiquitos")
public class SettlementController {

    private final SettlementService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar finiquitos (paginado)")
    public ResponseEntity<PagedResponse<SettlementResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long legalTerminationCauseId,
            @RequestParam(required = false) Long qualityOfWorkId,
            @RequestParam(required = false) Long safetyComplianceId,
            @RequestParam(required = false) Long noReHiredCauseId,
            @RequestParam(required = false) Boolean rehireEligible,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(search, status, legalTerminationCauseId,
                qualityOfWorkId, safetyComplianceId, noReHiredCauseId,
                rehireEligible, endDateFrom, endDateTo, createdFrom, createdTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finiquito por ID")
    public ResponseEntity<SettlementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear finiquito (con documentos opcionales)")
    public ResponseEntity<SettlementResponse> create(
            @RequestPart("data") @Valid SettlementRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar finiquito (con documentos opcionales)")
    public ResponseEntity<SettlementResponse> update(
            @RequestPart("data") @Valid UpdateSettlementRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(service.update(request, files));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar finiquitos a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"settlements.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }
}
