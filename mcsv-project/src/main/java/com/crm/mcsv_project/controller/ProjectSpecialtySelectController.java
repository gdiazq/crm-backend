package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/project-specialties")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class ProjectSpecialtySelectController {

    private final ProjectSpecialtyRepository projectSpecialtyRepository;

    @GetMapping
    @Operation(summary = "Especialidades de proyecto")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = projectSpecialtyRepository.findByActiveTrue().stream()
                .map(e -> new Item(e.getId(), e.getName())).toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
