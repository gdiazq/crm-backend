package com.crm.mcsv_rrhh.controller.profession;

import com.crm.mcsv_rrhh.dto.profession.ProfessionResponse;
import com.crm.mcsv_rrhh.service.profession.ProfessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/professions")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProfessionController {

    private final ProfessionService service;

    @GetMapping
    @Operation(summary = "Profesiones u oficios")
    public ResponseEntity<List<ProfessionResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
