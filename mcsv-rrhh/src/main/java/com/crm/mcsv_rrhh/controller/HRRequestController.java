package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.HRRequestResponse;
import com.crm.mcsv_rrhh.dto.RejectHRRequestRequest;
import com.crm.mcsv_rrhh.service.HRRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr-request")
@RequiredArgsConstructor
@Tag(name = "HR Requests", description = "Flujo de aprobación de solicitudes RRHH")
public class HRRequestController {

    private final HRRequestService hrRequestService;

    @GetMapping("/paged")
    @Operation(summary = "Listar solicitudes (paginado). idModule opcional para filtrar por empleado.")
    public ResponseEntity<Page<HRRequestResponse>> list(
            @RequestParam(required = false) Long idModule,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(hrRequestService.list(idModule, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una solicitud")
    public ResponseEntity<HRRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hrRequestService.getById(id));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Aprobar solicitud (paso supervisor o RRHH)")
    public ResponseEntity<HRRequestResponse> approve(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(hrRequestService.approve(id, userId));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rechazar solicitud")
    public ResponseEntity<HRRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectHRRequestRequest req) {
        return ResponseEntity.ok(hrRequestService.reject(id, req));
    }
}
