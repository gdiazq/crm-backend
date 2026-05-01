package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.LeaveTypeResponse;
import com.crm.mcsv_rrhh.repository.LeaveTypeRepository;
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

    private final LeaveTypeRepository repository;

    @GetMapping
    @Operation(summary = "Tipos de permiso activos")
    public ResponseEntity<List<LeaveTypeResponse>> getAll() {
        List<LeaveTypeResponse> result = repository.findByActiveTrue().stream()
                .map(e -> LeaveTypeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .paid(e.getPaid())
                        .requiresDocument(e.getRequiresDocument())
                        .requireApproval(e.getRequireApproval())
                        .maxDaysPerYear(e.getMaxDaysPerYear())
                        .build())
                .toList();
        return ResponseEntity.ok(result);
    }
}
