package com.crm.mcsv_rrhh.controller;

import com.crm.mcsv_rrhh.repository.MealTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/meal-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class MealTypeController {

    private final MealTypeRepository mealTypeRepository;

    @GetMapping
    @Operation(summary = "Tipos de colación")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = mealTypeRepository.findAll().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
