package com.crm.mcsv_rrhh.controller.contract;

import com.crm.mcsv_rrhh.dto.contract.ContractStatusResponse;
import com.crm.mcsv_rrhh.service.contract.ContractStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/contract-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ContractStatusController {

    private final ContractStatusService service;

    @GetMapping
    @Operation(summary = "Estados de contrato")
    public ResponseEntity<List<ContractStatusResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
