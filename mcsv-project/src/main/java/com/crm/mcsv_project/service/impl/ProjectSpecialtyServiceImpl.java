package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import com.crm.mcsv_project.service.ProjectSpecialtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectSpecialtyServiceImpl implements ProjectSpecialtyService {

    private final ProjectSpecialtyRepository repository;

    @Override
    public ProjectSpecialtyResponse create(ProjectSpecialtyRequest request) {
        if (repository.existsByName(request.getName()))
            throw new DuplicateResourceException("Ya existe una especialidad con el nombre: " + request.getName());

        ProjectSpecialty entity = ProjectSpecialty.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return toResponse(repository.save(entity));
    }

    @Override
    public ProjectSpecialtyResponse update(UpdateProjectSpecialtyRequest request) {
        ProjectSpecialty entity = repository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + request.getId()));

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getActive() != null) entity.setActive(request.getActive());

        return toResponse(repository.save(entity));
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        ProjectSpecialty entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id));
        entity.setActive(active);
        repository.save(entity);
    }

    @Override
    public ProjectSpecialtyResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada: " + id)));
    }

    @Override
    public PagedResponse<ProjectSpecialtyResponse> list(String search, Boolean active, Pageable pageable) {
        Page<ProjectSpecialty> page = repository.findAllWithFilters(pageable, search, active);
        long totalActive = repository.findAllWithFilters(Pageable.unpaged(), null, true).getTotalElements();
        return PagedResponse.of(page.map(this::toResponse), page.getTotalElements(), totalActive);
    }

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nombre,Descripción,Activo,Fecha Creación,Fecha Actualización\n");

        repository.findAll().forEach(e -> csv
                .append(e.getId()).append(",")
                .append(escape(e.getName())).append(",")
                .append(escape(e.getDescription())).append(",")
                .append(e.getActive()).append(",")
                .append(formatDate(e.getCreatedAt())).append(",")
                .append(formatDate(e.getUpdatedAt())).append("\n"));

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BulkImportResult importFromCsv(MultipartFile file) {
        List<BulkImportResult.RowError> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return BulkImportResult.builder().total(0).success(0).failed(0).errors(errors).build();
            }
            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> idx = buildHeaderIndex(headers);

            int iName = idx.getOrDefault("nombre", -1);
            int iDesc = idx.getOrDefault("descripción", idx.getOrDefault("descripcion", -1));
            int iActive = idx.getOrDefault("activo", -1);

            if (iName < 0) {
                errors.add(new BulkImportResult.RowError(1, "No se encontró la columna 'nombre' en el header"));
                return BulkImportResult.builder().total(0).success(0).failed(1).errors(errors).build();
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] cols = parseCsvLine(line);

                    String name = col(cols, iName);
                    if (name.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio");

                    String description = col(cols, iDesc);
                    String activeStr = col(cols, iActive);
                    Boolean active = activeStr.isEmpty() ? true : Boolean.parseBoolean(activeStr);

                    ProjectSpecialtyRequest request = new ProjectSpecialtyRequest();
                    request.setName(name);
                    request.setDescription(description.isEmpty() ? null : description);
                    request.setActive(active);

                    create(request);
                    success++;
                } catch (Exception e) {
                    errors.add(new BulkImportResult.RowError(row, e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Error reading CSV file for project specialties", e);
            errors.add(new BulkImportResult.RowError(0, "Error leyendo el archivo: " + e.getMessage()));
        }

        return BulkImportResult.builder()
                .total(total)
                .success(success)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            idx.put(headers[i].trim().toLowerCase(), i);
        }
        return idx;
    }

    private String col(String[] cols, int index) {
        if (index < 0 || index >= cols.length) return "";
        return cols[index].trim();
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private String formatDate(java.time.LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private ProjectSpecialtyResponse toResponse(ProjectSpecialty e) {
        return ProjectSpecialtyResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
