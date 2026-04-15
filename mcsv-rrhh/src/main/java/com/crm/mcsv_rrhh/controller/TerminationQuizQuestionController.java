package com.crm.mcsv_rrhh.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.dto.TerminationQuizQuestionResponse;
import com.crm.mcsv_rrhh.dto.UpdateTerminationQuizQuestionRequest;
import com.crm.mcsv_rrhh.service.TerminationQuizQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/termination-quiz-question")
@RequiredArgsConstructor
@Tag(name = "TerminationQuizQuestion", description = "Gestión de preguntas del cuestionario de finiquito")
public class TerminationQuizQuestionController {

    private final TerminationQuizQuestionService service;

    @GetMapping("/paged")
    @Operation(summary = "Listar preguntas (paginado)")
    public ResponseEntity<PagedResponse<TerminationQuizQuestionResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long questionGroupId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(search, active, questionGroupId, employeeId,
                createdFrom, createdTo, updatedFrom, updatedTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pregunta por ID")
    public ResponseEntity<TerminationQuizQuestionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear pregunta con opciones")
    public ResponseEntity<TerminationQuizQuestionResponse> create(
            @Valid @RequestBody TerminationQuizQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar pregunta y sus opciones")
    public ResponseEntity<TerminationQuizQuestionResponse> update(
            @Valid @RequestBody UpdateTerminationQuizQuestionRequest request) {
        return ResponseEntity.ok(service.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar pregunta")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        service.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }
}
