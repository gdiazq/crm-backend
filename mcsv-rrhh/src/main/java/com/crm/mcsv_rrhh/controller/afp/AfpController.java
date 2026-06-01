package com.crm.mcsv_rrhh.controller.afp;

import com.crm.mcsv_rrhh.dto.afp.AfpResponse;
import com.crm.mcsv_rrhh.service.afp.AfpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/afps")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class AfpController {

    private final AfpService service;

    @GetMapping
    @Operation(summary = "AFP (Administradoras de Fondos de Pensiones)")
    public ResponseEntity<List<AfpResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
