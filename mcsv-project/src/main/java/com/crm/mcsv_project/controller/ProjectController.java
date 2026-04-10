package com.crm.mcsv_project.controller;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectRequest;
import com.crm.mcsv_project.dto.ProjectResponse;
import com.crm.mcsv_project.dto.UpdateProjectRequest;
import com.crm.mcsv_project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Gestión de proyectos")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/paged")
    @Operation(summary = "Listar proyectos (paginado)")
    public ResponseEntity<PagedResponse<ProjectResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(projectService.list(search, active, typeId, statusId, specialtyId,
                createdFrom, createdTo, updatedFrom, updatedTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear proyecto")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar proyecto")
    public ResponseEntity<ProjectResponse> update(@Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar proyecto")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        projectService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar proyectos a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"projects.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(projectService.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar proyectos desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(projectService.importFromCsv(file));
    }
}
