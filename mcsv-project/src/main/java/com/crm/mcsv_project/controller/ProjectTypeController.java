package com.crm.mcsv_project.controller;

import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectTypeRequest;
import com.crm.mcsv_project.dto.ProjectTypeResponse;
import com.crm.mcsv_project.dto.UpdateProjectTypeRequest;
import com.crm.mcsv_project.service.ProjectTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/project-type")
@RequiredArgsConstructor
@Tag(name = "ProjectType", description = "Gestión de tipos de proyecto")
public class ProjectTypeController {

    private final ProjectTypeService projectTypeService;

    @GetMapping("/paged")
    @Operation(summary = "Listar tipos de proyecto (paginado)")
    public ResponseEntity<PagedResponse<ProjectTypeResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
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
        return ResponseEntity.ok(projectTypeService.list(search, active, createdFrom, createdTo, updatedFrom, updatedTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de proyecto por ID")
    public ResponseEntity<ProjectTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectTypeService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear tipo de proyecto")
    public ResponseEntity<ProjectTypeResponse> create(@Valid @RequestBody ProjectTypeRequest request) {
        return ResponseEntity.ok(projectTypeService.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar tipo de proyecto")
    public ResponseEntity<ProjectTypeResponse> update(@Valid @RequestBody UpdateProjectTypeRequest request) {
        return ResponseEntity.ok(projectTypeService.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar tipo de proyecto")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        projectTypeService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar tipos de proyecto a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"project-types.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(projectTypeService.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar tipos de proyecto desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(projectTypeService.importFromCsv(file));
    }
}
