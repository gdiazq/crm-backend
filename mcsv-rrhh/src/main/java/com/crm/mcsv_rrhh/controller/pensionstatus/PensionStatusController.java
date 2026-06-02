package com.crm.mcsv_rrhh.controller.pensionstatus;

import com.crm.mcsv_rrhh.dto.pensionstatus.PensionStatusResponse;
import com.crm.mcsv_rrhh.service.pensionstatus.PensionStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/pension-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class PensionStatusController {

    private final PensionStatusService service;

    @GetMapping
    @Operation(summary = "Sistemas de pensión")
    public ResponseEntity<List<PensionStatusResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
