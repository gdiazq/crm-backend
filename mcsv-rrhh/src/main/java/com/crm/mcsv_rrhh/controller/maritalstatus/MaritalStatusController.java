package com.crm.mcsv_rrhh.controller.maritalstatus;

import com.crm.mcsv_rrhh.dto.maritalstatus.MaritalStatusResponse;
import com.crm.mcsv_rrhh.service.maritalstatus.MaritalStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/marital-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class MaritalStatusController {

    private final MaritalStatusService service;

    @GetMapping
    @Operation(summary = "Estados civiles")
    public ResponseEntity<List<MaritalStatusResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
