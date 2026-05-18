package com.crm.mcsv_recruitment.controller;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_recruitment.dto.JobOpeningRequest;
import com.crm.mcsv_recruitment.dto.JobOpeningResponse;
import com.crm.mcsv_recruitment.dto.UpdateJobOpeningRequest;
import com.crm.mcsv_recruitment.service.JobOpeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/job-openings")
@RequiredArgsConstructor
@Tag(name = "JobOpening", description = "Vacantes / requisiciones de personal")
public class JobOpeningController {

    private final JobOpeningService jobOpeningService;

    @GetMapping("/paged")
    @Operation(summary = "Listado paginado de vacantes con filtros")
    public ResponseEntity<PagedResponse<JobOpeningResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Integer costCenter,
            @RequestParam(required = false) Long supervisorUserId,
            @RequestParam(required = false) LocalDate closeDateFrom,
            @RequestParam(required = false) LocalDate closeDateTo,
            @RequestParam(required = false) LocalDateTime createdFrom,
            @RequestParam(required = false) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = "asc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(jobOpeningService.list(search, statusId, costCenter, supervisorUserId,
                closeDateFrom, closeDateTo, createdFrom, createdTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de vacante por ID")
    public ResponseEntity<JobOpeningResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobOpeningService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Crear nueva vacante (estado inicial DRAFT)")
    public ResponseEntity<JobOpeningResponse> create(
            @Valid @RequestBody JobOpeningRequest request,
            @RequestHeader("x-user-id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobOpeningService.create(request, userId));
    }

    @PutMapping
    @Operation(summary = "Actualizar vacante existente")
    public ResponseEntity<JobOpeningResponse> update(
            @Valid @RequestBody UpdateJobOpeningRequest request,
            @RequestHeader("x-user-id") Long userId) {
        return ResponseEntity.ok(jobOpeningService.update(request, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar vacante (solo si está en DRAFT)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobOpeningService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
