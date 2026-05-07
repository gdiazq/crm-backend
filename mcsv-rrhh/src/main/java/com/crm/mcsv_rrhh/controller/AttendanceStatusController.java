package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.service.AttendanceStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance-statuses")
@RequiredArgsConstructor
@Tag(name = "AttendanceStatus", description = "Catálogo de estados base de asistencia")
public class AttendanceStatusController {

    private final AttendanceStatusService service;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de asistencia por ID")
    public ResponseEntity<AttendanceStatusResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar estado de asistencia")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        service.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/select")
    @Operation(summary = "Listar estados activos de asistencia")
    public ResponseEntity<List<AttendanceStatusResponse>> select() {
        return ResponseEntity.ok(service.selectActive());
    }
}
