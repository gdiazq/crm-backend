package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.ProfessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/professions")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProfessionController {

    private final ProfessionRepository professionRepository;

    @GetMapping
    @Operation(summary = "Profesiones u oficios")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = professionRepository.findAll().stream()
                .map(p -> new Item(p.getId(), p.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
