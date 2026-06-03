package com.crm.mcsv_rrhh.controller.identificationtype;

import com.crm.mcsv_rrhh.dto.identificationtype.IdentificationTypeResponse;
import com.crm.mcsv_rrhh.service.identificationtype.IdentificationTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/identification-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class IdentificationTypeController {

    private final IdentificationTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de identificación activos")
    public ResponseEntity<List<IdentificationTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
