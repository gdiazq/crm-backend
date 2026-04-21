package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.ContractAnnexRequest;
import com.crm.mcsv_rrhh.dto.ContractAnnexResponse;
import com.crm.mcsv_rrhh.dto.UpdateContractAnnexRequest;
import com.crm.mcsv_rrhh.service.ContractAnnexService;
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
import java.util.Set;

@RestController
@RequestMapping("/annexes")
@RequiredArgsConstructor
@Tag(name = "ContractAnnex", description = "Gestión de anexos de contrato")
public class ContractAnnexController {

    private static final Set<String> EMPLOYEE_SORT_FIELDS = Set.of("identification", "firstName", "paternalLastName");

    private final ContractAnnexService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar anexos (paginado)")
    public ResponseEntity<PagedResponse<ContractAnnexResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long annexTypeId,
            @RequestParam(required = false) Long contractId,
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

        String effectiveSortBy = "status".equals(sortBy) ? "currentStatusName" : sortBy;
        Sort sort = EMPLOYEE_SORT_FIELDS.contains(sortBy)
                ? Sort.unsorted()
                : (sortDir.equalsIgnoreCase("asc") ? Sort.by(effectiveSortBy).ascending() : Sort.by(effectiveSortBy).descending());
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(service.list(search, status, annexTypeId, contractId,
                dateFrom, dateTo, createdFrom, createdTo, updatedFrom, updatedTo, pageable, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener anexo por ID")
    public ResponseEntity<ContractAnnexResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear anexo de contrato (con documentos opcionales)")
    public ResponseEntity<ContractAnnexResponse> create(
            @RequestPart("data") @Valid ContractAnnexRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar anexo (genera HRRequest de actualización, con documentos opcionales)")
    public ResponseEntity<ContractAnnexResponse> update(
            @RequestPart("data") @Valid UpdateContractAnnexRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(service.update(request, files));
    }

    @DeleteMapping("/{id}/documents/{fileId}")
    @Operation(summary = "Eliminar documento adjunto del anexo")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestParam("userId") Long userId) {
        service.deleteDocument(id, fileId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar anexos a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"annexes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }

    @GetMapping("/select/by-contract/{contractId}")
    @Operation(summary = "Listar anexos de un contrato (sin paginar)")
    public ResponseEntity<List<ContractAnnexResponse>> findByContract(@PathVariable Long contractId) {
        return ResponseEntity.ok(service.findByContract(contractId));
    }
}
