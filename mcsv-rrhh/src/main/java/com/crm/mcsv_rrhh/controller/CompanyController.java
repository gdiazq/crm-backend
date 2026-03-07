package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.CompanyRepository;
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

    private final CompanyRepository companyRepository;

    @GetMapping
    @Operation(summary = "Empresas")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = companyRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
