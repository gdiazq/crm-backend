package com.crm.mcsv_rrhh.controller.commune;

import com.crm.mcsv_rrhh.dto.commune.CommuneResponse;
import com.crm.mcsv_rrhh.service.commune.CommuneService;
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
@RequestMapping("/select/communes")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class CommuneController {

    private final CommuneService service;

    @GetMapping
    @Operation(summary = "Comunas, filtrable por región")
    public ResponseEntity<List<CommuneResponse>> getAll(@RequestParam(required = false) Long regionId) {
        return ResponseEntity.ok(service.select(regionId));
    }
}
