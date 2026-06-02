package com.crm.mcsv_rrhh.controller.city;

import com.crm.mcsv_rrhh.dto.city.CityResponse;
import com.crm.mcsv_rrhh.service.city.CityService;
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

    private final CityService service;

    @GetMapping
    @Operation(summary = "Ciudades, filtrable por comuna")
    public ResponseEntity<List<CityResponse>> getAll(@RequestParam(required = false) Long communeId) {
        return ResponseEntity.ok(service.select(communeId));
    }
}
