package com.crm.mcsv_rrhh.controller.healthinsurance;

import com.crm.mcsv_rrhh.dto.healthinsurance.HealthInsuranceResponse;
import com.crm.mcsv_rrhh.service.healthinsurance.HealthInsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/health-insurances")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class HealthInsuranceController {

    private final HealthInsuranceService service;

    @GetMapping
    @Operation(summary = "Instituciones de salud (Fonasa / Isapres)")
    public ResponseEntity<List<HealthInsuranceResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
