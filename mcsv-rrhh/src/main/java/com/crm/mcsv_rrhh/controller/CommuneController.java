package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.CommuneRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/communes")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class CommuneController {

    private final CommuneRepository communeRepository;

    @GetMapping
    @Operation(summary = "Comunas, filtrable por región")
    public ResponseEntity<List<Item>> getAll(@RequestParam(required = false) Long regionId) {
        List<Item> result = (regionId != null
                ? communeRepository.findByRegionId(regionId)
                : communeRepository.findAll()).stream()
                .map(c -> new Item(c.getId(), c.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
