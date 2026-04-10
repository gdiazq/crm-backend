package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.client.PersonSelectItem;
import com.crm.mcsv_project.client.RrhhClient;
import com.crm.mcsv_project.client.UserClient;
import com.crm.common.dto.BulkImportResult;
import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectRequest;
import com.crm.mcsv_project.dto.ProjectResponse;
import com.crm.mcsv_project.dto.UpdateProjectRequest;
import com.crm.mcsv_project.entity.Project;
import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.mcsv_project.entity.ProjectStatus;
import com.crm.mcsv_project.entity.ProjectType;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectRepository;
import com.crm.mcsv_project.repository.ProjectSpecification;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import com.crm.mcsv_project.repository.ProjectStatusRepository;
import com.crm.mcsv_project.repository.ProjectTypeRepository;
import com.crm.common.util.CsvUtil;
import com.crm.common.util.DateRangeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements com.crm.mcsv_project.service.ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectSpecialtyRepository projectSpecialtyRepository;
    private final UserClient userClient;
    private final RrhhClient rrhhClient;

    // ─── List ─────────────────────────────────────────────────────────────────

    @Override
    public PagedResponse<ProjectResponse> list(String search, Boolean active,
                                                Long typeId, Long statusId, Long specialtyId,
                                                LocalDate createdFrom, LocalDate createdTo,
                                                LocalDate updatedFrom, LocalDate updatedTo,
                                                Pageable pageable) {
        Specification<Project> spec = ProjectSpecification.withFilters(
                search, active, typeId, statusId, specialtyId,
                DateRangeUtil.startOf(createdFrom), DateRangeUtil.endOf(createdTo),
                DateRangeUtil.startOf(updatedFrom), DateRangeUtil.endOf(updatedTo));

        Page<Project> page = projectRepository.findAll(spec, pageable);
        long totalActive = projectRepository.count(
                ProjectSpecification.withFilters(null, true, null, null, null, null, null, null, null));

        Map<Long, String> visitorMap      = fetchVisitorMap();
        Map<Long, String> supervisorMap   = fetchSupervisorMap();
        Map<Long, String> companyRepMap   = fetchCompanyRepMap();

        return PagedResponse.of(page.map(p -> toResponse(p, visitorMap, supervisorMap, companyRepMap)),
                page.getTotalElements(), totalActive);
    }

    // ─── GetById ──────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse getById(Long id) {
        Project project = findOrThrow(id);
        return toResponse(project, fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByCostCenter(request.getCostCenter()))
            throw new DuplicateResourceException("Ya existe un proyecto con el centro de costo: " + request.getCostCenter());

        Project project = Project.builder()
                .costCenter(request.getCostCenter())
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .type(resolveType(request.getTypeId()))
                .status(resolveStatus(request.getStatusId()))
                .specialty(resolveSpecialty(request.getSpecialtyId()))
                .visitorId(request.getVisitorId())
                .supervisorId(request.getSupervisorId())
                .companyRepresentativeIds(request.getCompanyRepresentativeIds() != null
                        ? request.getCompanyRepresentativeIds()
                        : new ArrayList<>())
                .startDate(request.getStartDate())
                .realStartDate(request.getRealStartDate())
                .endDate(request.getEndDate())
                .realEndDate(request.getRealEndDate())
                .active(true)
                .build();

        return toResponse(projectRepository.save(project),
                fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    public ProjectResponse update(UpdateProjectRequest request) {
        Project project = findOrThrow(request.getId());

        if (request.getCostCenter() != null &&
                !request.getCostCenter().equals(project.getCostCenter()) &&
                projectRepository.existsByCostCenterAndIdNot(request.getCostCenter(), request.getId()))
            throw new DuplicateResourceException("Ya existe un proyecto con el centro de costo: " + request.getCostCenter());

        if (request.getCostCenter() != null)   project.setCostCenter(request.getCostCenter());
        if (request.getName() != null)         project.setName(request.getName());
        if (request.getAddress() != null)      project.setAddress(request.getAddress());
        if (request.getDescription() != null)  project.setDescription(request.getDescription());
        if (request.getTypeId() != null)       project.setType(resolveType(request.getTypeId()));
        if (request.getStatusId() != null)     project.setStatus(resolveStatus(request.getStatusId()));
        if (request.getSpecialtyId() != null)  project.setSpecialty(resolveSpecialty(request.getSpecialtyId()));
        if (request.getVisitorId() != null)    project.setVisitorId(request.getVisitorId());
        if (request.getSupervisorId() != null) project.setSupervisorId(request.getSupervisorId());
        if (request.getCompanyRepresentativeIds() != null)
            project.setCompanyRepresentativeIds(request.getCompanyRepresentativeIds());
        if (request.getStartDate() != null)     project.setStartDate(request.getStartDate());
        if (request.getRealStartDate() != null) project.setRealStartDate(request.getRealStartDate());
        if (request.getEndDate() != null)       project.setEndDate(request.getEndDate());
        if (request.getRealEndDate() != null)   project.setRealEndDate(request.getRealEndDate());

        return toResponse(projectRepository.save(project),
                fetchVisitorMap(), fetchSupervisorMap(), fetchCompanyRepMap());
    }

    @Override
    public void updateStatus(Long id, Boolean active) {
        Project project = findOrThrow(id);
        project.setActive(active);
        projectRepository.save(project);
    }

    // ─── Export/Import ────────────────────────────────────────────────────────

    @Override
    public byte[] exportCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Centro de Costo,Nombre,Dirección,Descripción,Tipo ID,Estado ID,Especialidad ID," +
                   "Visitador ID,Supervisor ID,Fecha Inicio,Fecha Inicio Real,Fecha Fin,Fecha Fin Real," +
                   "Activo,Fecha Creación,Fecha Actualización\n");

        projectRepository.findAll().forEach(p -> csv
                .append(p.getId()).append(",")
                .append(p.getCostCenter()).append(",")
                .append(escape(p.getName())).append(",")
                .append(escape(p.getAddress())).append(",")
                .append(escape(p.getDescription())).append(",")
                .append(p.getType()      != null ? p.getType().getId()      : "").append(",")
                .append(p.getStatus()    != null ? p.getStatus().getId()    : "").append(",")
                .append(p.getSpecialty() != null ? p.getSpecialty().getId() : "").append(",")
                .append(p.getVisitorId()    != null ? p.getVisitorId()    : "").append(",")
                .append(p.getSupervisorId() != null ? p.getSupervisorId() : "").append(",")
                .append(p.getStartDate()     != null ? p.getStartDate()     : "").append(",")
                .append(p.getRealStartDate() != null ? p.getRealStartDate() : "").append(",")
                .append(p.getEndDate()       != null ? p.getEndDate()       : "").append(",")
                .append(p.getRealEndDate()   != null ? p.getRealEndDate()   : "").append(",")
                .append(p.getActive()).append(",")
                .append(formatDate(p.getCreatedAt())).append(",")
                .append(formatDate(p.getUpdatedAt())).append("\n"));

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
            if (headerLine == null)
                return BulkImportResult.builder().total(0).success(0).failed(0).errors(errors).build();

            String[] headers = CsvUtil.parseLine(headerLine);
            Map<String, Integer> idx = CsvUtil.headerIndex(headers);

            int iCostCenter  = idx.getOrDefault("centro de costo", -1);
            int iName        = idx.getOrDefault("nombre", -1);
            int iAddress     = idx.getOrDefault("dirección", idx.getOrDefault("direccion", -1));
            int iDescription = idx.getOrDefault("descripción", idx.getOrDefault("descripcion", -1));
            int iTypeId      = idx.getOrDefault("tipo id", -1);
            int iStatusId    = idx.getOrDefault("estado id", -1);
            int iSpecialtyId = idx.getOrDefault("especialidad id", -1);
            int iVisitorId   = idx.getOrDefault("visitador id", -1);
            int iSupervisorId= idx.getOrDefault("supervisor id", -1);
            int iCompanyReps = idx.getOrDefault("representantes ids", -1);
            int iStartDate   = idx.getOrDefault("fecha inicio", -1);
            int iEndDate     = idx.getOrDefault("fecha fin", -1);

            if (iCostCenter < 0 || iName < 0) {
                errors.add(new BulkImportResult.RowError(1, "Faltan columnas obligatorias: 'centro de costo' y/o 'nombre'"));
                return BulkImportResult.builder().total(0).success(0).failed(1).errors(errors).build();
            }

            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;
                total++;
                try {
                    String[] cols = CsvUtil.parseLine(line);

                    String costCenterStr = CsvUtil.col(cols, iCostCenter);
                    String name          = CsvUtil.col(cols, iName);

                    if (costCenterStr.isEmpty()) throw new IllegalArgumentException("El centro de costo es obligatorio");
                    if (name.isEmpty())          throw new IllegalArgumentException("El nombre es obligatorio");

                    ProjectRequest request = new ProjectRequest();
                    request.setCostCenter(Integer.parseInt(costCenterStr));
                    request.setName(name);
                    request.setAddress(nullIfEmpty(CsvUtil.col(cols, iAddress)));
                    request.setDescription(nullIfEmpty(CsvUtil.col(cols, iDescription)));
                    request.setTypeId(parseLong(CsvUtil.col(cols, iTypeId)));
                    request.setStatusId(parseLong(CsvUtil.col(cols, iStatusId)));
                    request.setSpecialtyId(parseLong(CsvUtil.col(cols, iSpecialtyId)));
                    request.setVisitorId(parseLong(CsvUtil.col(cols, iVisitorId)));
                    request.setSupervisorId(parseLong(CsvUtil.col(cols, iSupervisorId)));
                    request.setCompanyRepresentativeIds(parseLongList(CsvUtil.col(cols, iCompanyReps)));
                    request.setStartDate(parseDate(CsvUtil.col(cols, iStartDate)));
                    request.setEndDate(parseDate(CsvUtil.col(cols, iEndDate)));

                    create(request);
                    success++;
                } catch (Exception e) {
                    errors.add(new BulkImportResult.RowError(row, e.getMessage()));
                }
            }
        } catch (Exception e) {
            log.error("Error leyendo CSV de proyectos", e);
            errors.add(new BulkImportResult.RowError(0, "Error leyendo el archivo: " + e.getMessage()));
        }

        return BulkImportResult.builder()
                .total(total)
                .success(success)
                .failed(errors.size())
                .errors(errors)
                .build();
    }

    // ─── Feign helpers ────────────────────────────────────────────────────────

    private Map<Long, String> fetchVisitorMap() {
        try {
            return rrhhClient.getVisitors().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener visitadores: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> fetchSupervisorMap() {
        try {
            return rrhhClient.getSupervisors().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener supervisores: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> fetchCompanyRepMap() {
        try {
            return rrhhClient.getCompanyRepresentatives().stream()
                    .collect(Collectors.toMap(PersonSelectItem::id, PersonSelectItem::name));
        } catch (Exception e) {
            log.warn("No se pudieron obtener representantes de empresa: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private ProjectResponse toResponse(Project p,
                                        Map<Long, String> visitorMap,
                                        Map<Long, String> supervisorMap,
                                        Map<Long, String> companyRepMap) {
        return ProjectResponse.builder()
                .id(p.getId())
                .costCenter(p.getCostCenter())
                .name(p.getName())
                .address(p.getAddress())
                .description(p.getDescription())
                .typeId(p.getType() != null ? p.getType().getId() : null)
                .typeName(p.getType() != null ? p.getType().getName() : null)
                .statusId(p.getStatus() != null ? p.getStatus().getId() : null)
                .statusName(p.getStatus() != null ? p.getStatus().getName() : null)
                .specialtyId(p.getSpecialty() != null ? p.getSpecialty().getId() : null)
                .specialtyName(p.getSpecialty() != null ? p.getSpecialty().getName() : null)
                .visitorId(p.getVisitorId())
                .visitorName(p.getVisitorId() != null ? visitorMap.getOrDefault(p.getVisitorId(), null) : null)
                .supervisorId(p.getSupervisorId())
                .supervisorName(p.getSupervisorId() != null ? supervisorMap.getOrDefault(p.getSupervisorId(), null) : null)
                .companyRepresentativeIds(p.getCompanyRepresentativeIds())
                .companyRepresentativeNames(p.getCompanyRepresentativeIds() != null
                        ? p.getCompanyRepresentativeIds().stream()
                                .map(id -> companyRepMap.getOrDefault(id, null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                        : Collections.emptyList())
                .startDate(p.getStartDate())
                .realStartDate(p.getRealStartDate())
                .endDate(p.getEndDate())
                .realEndDate(p.getRealEndDate())
                .active(p.getActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Project findOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));
    }

    private ProjectType resolveType(Long typeId) {
        if (typeId == null) return null;
        return projectTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proyecto no encontrado: " + typeId));
    }

    private ProjectStatus resolveStatus(Long statusId) {
        if (statusId == null) return null;
        return projectStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de proyecto no encontrado: " + statusId));
    }

    private ProjectSpecialty resolveSpecialty(Long specialtyId) {
        if (specialtyId == null) return null;
        return projectSpecialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad de proyecto no encontrada: " + specialtyId));
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private String nullIfEmpty(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return Long.parseLong(value); } catch (NumberFormatException e) { return null; }
    }

    private List<Long> parseLongList(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        List<Long> result = new ArrayList<>();
        for (String part : value.split("\\|")) {
            Long id = parseLong(part.trim());
            if (id != null) result.add(id);
        }
        return result;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return LocalDate.parse(value); } catch (Exception e) { return null; }
    }
}
