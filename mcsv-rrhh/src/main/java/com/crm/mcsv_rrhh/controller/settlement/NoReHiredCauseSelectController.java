package com.crm.mcsv_rrhh.controller.settlement;

import com.crm.mcsv_rrhh.dto.settlement.NoReHiredCauseSelectResponse;
import com.crm.mcsv_rrhh.service.settlement.NoReHiredCauseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/no-re-hired-causes")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class NoReHiredCauseSelectController {

    private final NoReHiredCauseService service;

    @GetMapping
    @Operation(summary = "Causas de no recontratación activas")
    public ResponseEntity<List<NoReHiredCauseSelectResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
