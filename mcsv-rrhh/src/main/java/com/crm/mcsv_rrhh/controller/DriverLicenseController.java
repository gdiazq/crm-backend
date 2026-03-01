package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.DriverLicenseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/driver-licenses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class DriverLicenseController {

    private final DriverLicenseRepository driverLicenseRepository;

    @GetMapping
    @Operation(summary = "Tipos de licencia de conducir")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = driverLicenseRepository.findAll().stream()
                .map(d -> new Item(d.getId(), d.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
