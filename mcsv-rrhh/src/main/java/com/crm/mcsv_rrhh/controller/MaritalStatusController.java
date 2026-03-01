package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.MaritalStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/marital-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class MaritalStatusController {

    private final MaritalStatusRepository maritalStatusRepository;

    @GetMapping
    @Operation(summary = "Estados civiles")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = maritalStatusRepository.findAll().stream()
                .map(m -> new Item(m.getId(), m.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
