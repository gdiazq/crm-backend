package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.RetirementStatusRepository;
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

    private final RetirementStatusRepository retirementStatusRepository;

    @GetMapping
    @Operation(summary = "Estados de jubilación")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = retirementStatusRepository.findAll().stream()
                .map(r -> new Item(r.getId(), r.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
