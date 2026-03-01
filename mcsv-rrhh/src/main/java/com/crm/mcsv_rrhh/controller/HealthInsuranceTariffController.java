package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.HealthInsuranceTariffRepository;
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

    private final HealthInsuranceTariffRepository healthInsuranceTariffRepository;

    @GetMapping
    @Operation(summary = "Tarifas de salud")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = healthInsuranceTariffRepository.findAll().stream()
                .map(h -> new Item(h.getId(), h.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
