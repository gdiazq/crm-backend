package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.SafetyComplianceRepository;
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

    private final SafetyComplianceRepository repository;

    @GetMapping
    @Operation(summary = "Categorías de cumplimiento de seguridad activas")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = repository.findByActiveTrue().stream()
                .map(e -> new Item(e.getId(), e.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
