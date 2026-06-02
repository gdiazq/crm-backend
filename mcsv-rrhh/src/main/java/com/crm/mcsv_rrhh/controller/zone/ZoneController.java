package com.crm.mcsv_rrhh.controller.zone;

import com.crm.mcsv_rrhh.dto.zone.ZoneResponse;
import com.crm.mcsv_rrhh.service.zone.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/zones")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ZoneController {

    private final ZoneService service;

    @GetMapping
    @Operation(summary = "Zonas / Áreas organizacionales")
    public ResponseEntity<List<ZoneResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
