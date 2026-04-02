package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationAgreementResponse;
import com.crm.mcsv_rrhh.service.TerminationAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/termination-agreement")
@RequiredArgsConstructor
@Tag(name = "TerminationAgreement", description = "Gestión de finiquitos")
public class TerminationAgreementController {

    private final TerminationAgreementService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar finiquitos (paginado)")
    public ResponseEntity<PagedResponse<TerminationAgreementResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long legalTerminationCauseId,
            @RequestParam(required = false) Boolean rehireEligible,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(search, status, employeeId, legalTerminationCauseId,
                rehireEligible, endDateFrom, endDateTo, createdFrom, createdTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener finiquito por ID")
    public ResponseEntity<TerminationAgreementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}
