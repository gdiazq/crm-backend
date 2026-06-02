package com.crm.mcsv_rrhh.controller.region;

import com.crm.mcsv_rrhh.dto.region.RegionResponse;
import com.crm.mcsv_rrhh.service.region.RegionService;
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

    private final RegionService service;

    @GetMapping
    @Operation(summary = "Regiones de Chile")
    public ResponseEntity<List<RegionResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
