package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contract")
@RequiredArgsConstructor
@Tag(name = "Contracts", description = "Gestión de contratos laborales")
public class ContractController {

    private final ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "Crear contrato para un empleado")
    public ResponseEntity<ContractDetailResponse> create(@Valid @RequestBody CreateContractRequest request) {
        return ResponseEntity.ok(contractService.createContract(request));
    }
}
