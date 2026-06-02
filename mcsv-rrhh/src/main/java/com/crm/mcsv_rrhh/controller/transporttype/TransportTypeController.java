package com.crm.mcsv_rrhh.controller.transporttype;

import com.crm.mcsv_rrhh.dto.transporttype.TransportTypeResponse;
import com.crm.mcsv_rrhh.service.transporttype.TransportTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/transport-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class TransportTypeController {

    private final TransportTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de movilización")
    public ResponseEntity<List<TransportTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
