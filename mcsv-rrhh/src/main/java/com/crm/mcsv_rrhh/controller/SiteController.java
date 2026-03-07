package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.SiteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/sites")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class SiteController {

    private final SiteRepository siteRepository;

    @GetMapping
    @Operation(summary = "Sedes físicas")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = siteRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
