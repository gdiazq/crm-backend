package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.repository.ProjectStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/project-statuses")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProjectStatusSelectController {

    private final ProjectStatusRepository projectStatusRepository;

    @GetMapping
    @Operation(summary = "Estados de proyecto")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = projectStatusRepository.findByActiveTrue().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
