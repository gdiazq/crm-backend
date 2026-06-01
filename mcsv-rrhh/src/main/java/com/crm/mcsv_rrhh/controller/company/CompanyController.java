package com.crm.mcsv_rrhh.controller.company;

import com.crm.mcsv_rrhh.dto.company.CompanyResponse;
import com.crm.mcsv_rrhh.service.company.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/companies")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class CompanyController {

    private final CompanyService service;

    @GetMapping
    @Operation(summary = "Empresas")
    public ResponseEntity<List<CompanyResponse>> getAll() {
        return ResponseEntity.ok(service.selectAll());
    }
}
