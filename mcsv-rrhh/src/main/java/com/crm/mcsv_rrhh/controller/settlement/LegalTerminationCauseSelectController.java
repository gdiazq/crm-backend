package com.crm.mcsv_rrhh.controller.settlement;

import com.crm.mcsv_rrhh.dto.settlement.LegalTerminationCauseSelectResponse;
import com.crm.mcsv_rrhh.service.settlement.LegalTerminationCauseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/legal-termination-causes")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class LegalTerminationCauseSelectController {

    private final LegalTerminationCauseService service;

    @GetMapping
    @Operation(summary = "Causales legales de término activas")
    public ResponseEntity<List<LegalTerminationCauseSelectResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
