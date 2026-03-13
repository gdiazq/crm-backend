package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import com.crm.mcsv_project.service.ProjectSpecialtyService;
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

import java.util.Map;

@RestController
@RequestMapping("/project-specialty")
@RequiredArgsConstructor
@Tag(name = "ProjectSpecialty", description = "Gestión de especialidades de proyecto")
public class ProjectSpecialtyController {

    private final ProjectSpecialtyService projectSpecialtyService;

    @GetMapping("/paged")
    @Operation(summary = "Listar especialidades de proyecto (paginado)")
    public ResponseEntity<PagedResponse<ProjectSpecialtyResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(projectSpecialtyService.list(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener especialidad de proyecto por ID")
    public ResponseEntity<ProjectSpecialtyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectSpecialtyService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear especialidad de proyecto")
    public ResponseEntity<ProjectSpecialtyResponse> create(@Valid @RequestBody ProjectSpecialtyRequest request) {
        return ResponseEntity.ok(projectSpecialtyService.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar especialidad de proyecto")
    public ResponseEntity<ProjectSpecialtyResponse> update(@Valid @RequestBody UpdateProjectSpecialtyRequest request) {
        return ResponseEntity.ok(projectSpecialtyService.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar especialidad de proyecto")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        projectSpecialtyService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar especialidades de proyecto a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"project-specialties.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(projectSpecialtyService.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar especialidades de proyecto desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(projectSpecialtyService.importFromCsv(file));
    }
}
