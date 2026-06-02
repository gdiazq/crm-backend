package com.crm.mcsv_rrhh.controller.laborunion;

import com.crm.mcsv_rrhh.dto.laborunion.LaborUnionResponse;
import com.crm.mcsv_rrhh.service.laborunion.LaborUnionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/labor-unions")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class LaborUnionController {

    private final LaborUnionService service;

    @GetMapping
    @Operation(summary = "Sindicatos")
    public ResponseEntity<List<LaborUnionResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
