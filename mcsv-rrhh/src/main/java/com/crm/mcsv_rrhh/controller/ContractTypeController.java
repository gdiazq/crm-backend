package com.crm.mcsv_rrhh.controller;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_rrhh.repository.ContractTypeRepository;
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

    private final ContractTypeRepository contractTypeRepository;

    @GetMapping
    @Operation(summary = "Tipos de contrato")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = contractTypeRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tipo de contrato por ID")
    public ResponseEntity<Item> getById(@PathVariable Long id) {
        return contractTypeRepository.findById(id)
                .map(e -> ResponseEntity.ok(new Item(e.getId(), e.getName())))
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de contrato no encontrado: " + id));
    }

    record Item(Long id, String name) {}
}
