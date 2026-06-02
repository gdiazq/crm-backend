package com.crm.mcsv_rrhh.controller.familyallowancetier;

import com.crm.mcsv_rrhh.dto.familyallowancetier.FamilyAllowanceTierResponse;
import com.crm.mcsv_rrhh.service.familyallowancetier.FamilyAllowanceTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/family-allowance-tiers")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class FamilyAllowanceTierController {

    private final FamilyAllowanceTierService service;

    @GetMapping
    @Operation(summary = "Tramos de asignación familiar")
    public ResponseEntity<List<FamilyAllowanceTierResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
