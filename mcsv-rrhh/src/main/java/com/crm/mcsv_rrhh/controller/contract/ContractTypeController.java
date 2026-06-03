package com.crm.mcsv_rrhh.controller.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractTypeResponse;
import com.crm.mcsv_rrhh.service.contract.ContractTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/contract-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ContractTypeController {

    private final ContractTypeService service;

    @GetMapping
    @Operation(summary = "Tipos de contrato")
    public ResponseEntity<List<ContractTypeResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tipo de contrato por ID")
    public ResponseEntity<ContractTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
