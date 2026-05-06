package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.AttendanceMarkRequest;
import com.crm.mcsv_rrhh.dto.AttendanceMarkResponse;
import com.crm.mcsv_rrhh.dto.AttendanceMarkTypeSelectItem;
import com.crm.mcsv_rrhh.dto.UpdateAttendanceMarkRequest;
import com.crm.mcsv_rrhh.service.AttendanceMarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance-marks")
@RequiredArgsConstructor
@Tag(name = "Attendance Marks", description = "Marcajes individuales de entrada y salida")
public class AttendanceMarkController {

    private final AttendanceMarkService service;

    @PostMapping("/create")
    @Operation(summary = "Registrar marcaje individual de asistencia")
    public ResponseEntity<AttendanceMarkResponse> create(@Valid @RequestBody AttendanceMarkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar marcaje individual de asistencia")
    public ResponseEntity<AttendanceMarkResponse> update(@Valid @RequestBody UpdateAttendanceMarkRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @GetMapping("/by-attendance/{attendanceId}")
    @Operation(summary = "Listar marcajes por asistencia diaria")
    public ResponseEntity<List<AttendanceMarkResponse>> byAttendance(@PathVariable Long attendanceId) {
        return ResponseEntity.ok(service.findByAttendance(attendanceId));
    }

    @GetMapping("/select/types")
    @Operation(summary = "Selector de tipos de marcaje")
    public ResponseEntity<List<AttendanceMarkTypeSelectItem>> selectTypes() {
        return ResponseEntity.ok(service.findMarkTypes());
    }
}
