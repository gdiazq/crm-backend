package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectStatusRequest;
import com.crm.mcsv_project.dto.ProjectStatusResponse;
import com.crm.mcsv_project.dto.UpdateProjectStatusRequest;
import com.crm.mcsv_project.service.ProjectStatusService;
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
@RequestMapping("/project-status")
@RequiredArgsConstructor
@Tag(name = "ProjectStatus", description = "Gestión de estados de proyecto")
public class ProjectStatusController {

    private final ProjectStatusService projectStatusService;

    @GetMapping("/paged")
    @Operation(summary = "Listar estados de proyecto (paginado)")
    public ResponseEntity<PagedResponse<ProjectStatusResponse>> paged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(projectStatusService.list(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de proyecto por ID")
    public ResponseEntity<ProjectStatusResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectStatusService.getById(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Crear estado de proyecto")
    public ResponseEntity<ProjectStatusResponse> create(@Valid @RequestBody ProjectStatusRequest request) {
        return ResponseEntity.ok(projectStatusService.create(request));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar estado de proyecto")
    public ResponseEntity<ProjectStatusResponse> update(@Valid @RequestBody UpdateProjectStatusRequest request) {
        return ResponseEntity.ok(projectStatusService.update(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Habilitar/deshabilitar estado de proyecto")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        projectStatusService.updateStatus(id, body.get("active"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar estados de proyecto a CSV")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"project-statuses.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(projectStatusService.exportCsv());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar estados de proyecto desde CSV")
    public ResponseEntity<BulkImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(projectStatusService.importFromCsv(file));
    }
}
