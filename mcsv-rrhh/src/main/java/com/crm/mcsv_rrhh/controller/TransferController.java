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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear traspaso (con documentos opcionales)")
    public ResponseEntity<TransferResponse> create(
            @RequestPart("data") @Valid TransferRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, files));
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar traspaso (genera HRRequest de actualización, con documentos opcionales)")
    public ResponseEntity<TransferResponse> update(
            @RequestPart("data") @Valid UpdateTransferRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(service.update(request, files));
    }

    @DeleteMapping("/{id}/documents/{fileId}")
    @Operation(summary = "Eliminar documento adjunto del traspaso")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long fileId,
            @RequestParam("userId") Long userId) {
        service.deleteDocument(id, fileId, userId);
        return ResponseEntity.ok().build();
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
