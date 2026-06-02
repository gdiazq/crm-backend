package com.crm.mcsv_rrhh.controller.contractannex;

import com.crm.mcsv_rrhh.dto.contractannex.ContractAnnexTypeResponse;
import com.crm.mcsv_rrhh.service.contractannex.ContractAnnexTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/contract-annex-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ContractAnnexTypeSelectController {

    private final ContractAnnexTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de anexo de contrato activos")
    public ResponseEntity<List<ContractAnnexTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectActive());
    }
}
