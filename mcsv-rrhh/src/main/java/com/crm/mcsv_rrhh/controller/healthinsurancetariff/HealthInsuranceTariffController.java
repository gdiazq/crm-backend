package com.crm.mcsv_rrhh.controller.healthinsurancetariff;

import com.crm.mcsv_rrhh.dto.healthinsurancetariff.HealthInsuranceTariffResponse;
import com.crm.mcsv_rrhh.service.healthinsurancetariff.HealthInsuranceTariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/health-insurance-tariffs")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class HealthInsuranceTariffController {

    private final HealthInsuranceTariffService service;

    @GetMapping
    @Operation(summary = "Tarifas de salud")
    public ResponseEntity<List<HealthInsuranceTariffResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
