package com.crm.mcsv_rrhh.controller.identificationtype;

import com.crm.mcsv_rrhh.repository.identificationtype.IdentificationTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/select/identification-types")
@RequiredArgsConstructor
@Tag(name = "Selectors", description = "Endpoints para selects del frontend")
public class IdentificationTypeController {

    private final IdentificationTypeRepository identificationTypeRepository;

    @GetMapping
    @Operation(summary = "Tipos de identificación activos")
    public ResponseEntity<List<Item>> getAll() {
        List<Item> result = identificationTypeRepository.findByStatusTrue().stream()
                .map(t -> new Item(t.getId(), t.getName()))
                .toList();
        return ResponseEntity.ok(result);
    }

    record Item(Long id, String name) {}
}
