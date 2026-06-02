package com.crm.mcsv_rrhh.controller.retirementstatus;

import com.crm.mcsv_rrhh.dto.retirementstatus.RetirementStatusResponse;
import com.crm.mcsv_rrhh.service.retirementstatus.RetirementStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/retirement-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class RetirementStatusController {

    private final RetirementStatusService service;

    @GetMapping
    @Operation(summary = "Estados de jubilación")
    public ResponseEntity<List<RetirementStatusResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
