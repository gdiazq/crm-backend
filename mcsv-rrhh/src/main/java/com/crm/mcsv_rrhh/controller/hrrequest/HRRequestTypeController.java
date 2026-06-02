package com.crm.mcsv_rrhh.controller.hrrequest;

import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestTypeResponse;
import com.crm.mcsv_rrhh.service.hrrequest.HRRequestTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/hr-request-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class HRRequestTypeController {

    private final HRRequestTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de solicitud RRHH")
    public ResponseEntity<List<HRRequestTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
