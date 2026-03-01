package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.NationalityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/nationalities")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class NationalityController {

    private final NationalityRepository nationalityRepository;

    @GetMapping
    @Operation(summary = "Nacionalidades")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = nationalityRepository.findAll().stream()
                .map(n -> new Item(n.getId(), n.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
