package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.ContractDetailResponse;
import com.crm.mcsv_rrhh.dto.ContractResponse;
import com.crm.mcsv_rrhh.dto.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

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

    @GetMapping("/detail/{id}")
    @Operation(summary = "Obtener detalle de contrato por ID")
    public ResponseEntity<ContractDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getById(id));
    }

    @GetMapping("/paged")
    @Operation(summary = "Listar contratos (paginado)")
    public ResponseEntity<PagedResponse<ContractResponse>> paged(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContractResponse> result = contractService.list(employeeId, statusId, createdFrom, createdTo, pageable);
        Map<String, Long> stats = contractService.getStats(employeeId);

        return ResponseEntity.ok(PagedResponse.of(result, stats.get("total"), stats.get("active")));
    }
}
