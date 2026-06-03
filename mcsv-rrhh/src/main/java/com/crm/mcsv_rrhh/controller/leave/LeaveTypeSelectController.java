package com.crm.mcsv_rrhh.controller.leave;

import com.crm.mcsv_rrhh.dto.leave.LeaveTypeResponse;
import com.crm.mcsv_rrhh.service.leave.LeaveTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/leave-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class LeaveTypeSelectController {

    private final LeaveTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de permiso activos")
    public ResponseEntity<List<LeaveTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
