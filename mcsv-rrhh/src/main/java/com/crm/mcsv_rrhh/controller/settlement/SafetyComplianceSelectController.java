package com.crm.mcsv_rrhh.controller.settlement;

import com.crm.mcsv_rrhh.dto.settlement.SafetyComplianceSelectResponse;
import com.crm.mcsv_rrhh.service.settlement.SafetyComplianceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/safety-compliances")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class SafetyComplianceSelectController {

    private final SafetyComplianceService service;

    @GetMapping
    @Operation(summary = "Categorías de cumplimiento de seguridad activas")
    public ResponseEntity<List<SafetyComplianceSelectResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
