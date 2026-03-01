package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.AfpRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/afps")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class AfpController {

    private final AfpRepository afpRepository;

    @GetMapping
    @Operation(summary = "AFP (Administradoras de Fondos de Pensiones)")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = afpRepository.findAll().stream()
                .map(a -> new Item(a.getId(), a.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
