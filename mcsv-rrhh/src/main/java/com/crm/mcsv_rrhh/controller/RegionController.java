package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.RegionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/regions")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class RegionController {

    private final RegionRepository regionRepository;

    @GetMapping
    @Operation(summary = "Regiones de Chile")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = regionRepository.findAll().stream()
                .map(r -> new Item(r.getId(), r.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
