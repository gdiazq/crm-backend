package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TransferRequest;
import com.crm.mcsv_rrhh.dto.TransferResponse;
import com.crm.mcsv_rrhh.dto.UpdateTransferRequest;
import com.crm.mcsv_rrhh.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer", description = "Gestión de traspasos de centro de costo")
public class TransferController {

    private final TransferService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar traspasos (paginado)")
    public ResponseEntity<PagedResponse<TransferResponse>> paged(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(employeeId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener traspaso por ID")
    public ResponseEntity<TransferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear traspaso")
    public ResponseEntity<TransferResponse> create(@RequestBody @Valid TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar traspaso (genera HRRequest de actualización)")
    public ResponseEntity<TransferResponse> update(@RequestBody @Valid UpdateTransferRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar traspasos a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"transfers.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(service.exportCsv());
    }
}
