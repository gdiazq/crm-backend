package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.CityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/cities")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class CityController {

    private final CityRepository cityRepository;

    @GetMapping
    @Operation(summary = "Ciudades, filtrable por comuna")
    public ResponseEntity<List<Item>> getAll(@RequestParam(required = false) Long communeId) {
        List<Item> result = (communeId != null
                ? cityRepository.findByCommuneId(communeId)
                : cityRepository.findAll()).stream()
                .map(c -> new Item(c.getId(), c.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
