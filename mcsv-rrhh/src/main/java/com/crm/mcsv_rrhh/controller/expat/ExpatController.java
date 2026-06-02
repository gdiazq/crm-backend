package com.crm.mcsv_rrhh.controller.expat;

import com.crm.mcsv_rrhh.dto.expat.ExpatResponse;
import com.crm.mcsv_rrhh.service.expat.ExpatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/expats")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ExpatController {

    private final ExpatService service;

    @GetMapping
    @Operation(summary = "Condición de expatriado")
    public ResponseEntity<List<ExpatResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
