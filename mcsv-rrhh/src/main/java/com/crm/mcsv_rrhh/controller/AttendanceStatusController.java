package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.AttendanceStatusResponse;
import com.crm.mcsv_rrhh.service.AttendanceStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attendance-statuses")
@RequiredArgsConstructor
@Tag(name = "AttendanceStatus", description = "Catálogo de estados base de asistencia")
public class AttendanceStatusController {

    private final AttendanceStatusService service;

    @GetMapping("/select")
    @Operation(summary = "Listar estados activos de asistencia")
    public ResponseEntity<List<AttendanceStatusResponse>> select() {
        return ResponseEntity.ok(service.selectActive());
    }
}
