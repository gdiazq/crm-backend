package com.crm.mcsv_project.service.impl;

import com.crm.common.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectSpecialtyServiceImpl Tests")
class ProjectSpecialtyServiceImplTest {

    @Mock
    private ProjectSpecialtyRepository repository;

    @InjectMocks
    private ProjectSpecialtyServiceImpl service;

    private ProjectSpecialty specialty;
    private static final Long ID = 1L;
    private static final String NAME = "Ingeniería Civil";
    private static final String DESC = "Especialidad en construcción";

    @BeforeEach
    void setUp() {
        specialty = ProjectSpecialty.builder()
                .id(ID)
                .name(NAME)
                .description(DESC)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ---------------------------------------------------------------
    // create
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("happy path: saves and returns response")
        void create_success() {
            ProjectSpecialtyRequest req = new ProjectSpecialtyRequest(NAME, DESC);
            when(repository.existsByName(NAME)).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(specialty);

            ProjectSpecialtyResponse res = service.create(req);

            assertThat(res.getId()).isEqualTo(ID);
            assertThat(res.getName()).isEqualTo(NAME);
            assertThat(res.getDescription()).isEqualTo(DESC);
            assertThat(res.getActive()).isTrue();

            ArgumentCaptor<ProjectSpecialty> captor = ArgumentCaptor.forClass(ProjectSpecialty.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo(NAME);
            assertThat(captor.getValue().getActive()).isTrue();
        }

        @Test
        @DisplayName("throws DuplicateResourceException when name already exists")
        void create_duplicate_throws() {
            ProjectSpecialtyRequest req = new ProjectSpecialtyRequest(NAME, DESC);
            when(repository.existsByName(NAME)).thenReturn(true);

            assertThatThrownBy(() -> service.create(req))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(NAME);

            verify(repository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("happy path: updates name and description")
        void update_success() {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(ID, "Nuevo Nombre", "Nueva Desc");
            when(repository.findById(ID)).thenReturn(Optional.of(specialty));
            when(repository.save(specialty)).thenReturn(specialty);

            ProjectSpecialtyResponse res = service.update(req);

            assertThat(res.getName()).isEqualTo("Nuevo Nombre");
            verify(repository).save(specialty);
        }

        @Test
        @DisplayName("updates only non-null fields (partial update)")
        void update_partialFields() {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(ID, null, "Solo desc");
            when(repository.findById(ID)).thenReturn(Optional.of(specialty));
            when(repository.save(specialty)).thenReturn(specialty);

            service.update(req);

            // name should be unchanged
            assertThat(specialty.getName()).isEqualTo(NAME);
            assertThat(specialty.getDescription()).isEqualTo("Solo desc");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when id not found")
        void update_notFound_throws() {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(99L, "X", null);
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ---------------------------------------------------------------
    // updateStatus
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("sets active=false and saves")
        void updateStatus_deactivate() {
            when(repository.findById(ID)).thenReturn(Optional.of(specialty));

            service.updateStatus(ID, false);

            assertThat(specialty.getActive()).isFalse();
            verify(repository).save(specialty);
        }

        @Test
        @DisplayName("sets active=true and saves")
        void updateStatus_activate() {
            specialty.setActive(false);
            when(repository.findById(ID)).thenReturn(Optional.of(specialty));

            service.updateStatus(ID, true);

            assertThat(specialty.getActive()).isTrue();
            verify(repository).save(specialty);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when id not found")
        void updateStatus_notFound_throws() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(99L, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ---------------------------------------------------------------
    // getById
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns response for existing id")
        void getById_found() {
            when(repository.findById(ID)).thenReturn(Optional.of(specialty));

            ProjectSpecialtyResponse res = service.getById(ID);

            assertThat(res.getId()).isEqualTo(ID);
            assertThat(res.getName()).isEqualTo(NAME);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown id")
        void getById_notFound_throws() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ---------------------------------------------------------------
    // list
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("list()")
    class ListPaged {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("returns PagedResponse with correct metadata")
        void list_withAllNullFilters() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<ProjectSpecialty> page = new PageImpl<>(List.of(specialty), pageable, 1L);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(repository.count(any(Specification.class))).thenReturn(1L);

            PagedResponse<ProjectSpecialtyResponse> result =
                    service.list(null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getActive()).isEqualTo(1L);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("passes date range filters correctly")
        void list_withDateFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProjectSpecialty> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(repository.count(any(Specification.class))).thenReturn(0L);

            LocalDate from = LocalDate.of(2024, 1, 1);
            LocalDate to = LocalDate.of(2024, 12, 31);

            PagedResponse<ProjectSpecialtyResponse> result =
                    service.list("busqueda", true, from, to, from, to, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("returns empty page when no results")
        void list_emptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProjectSpecialty> emptyPage = Page.empty(pageable);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
            when(repository.count(any(Specification.class))).thenReturn(0L);

            PagedResponse<ProjectSpecialtyResponse> result =
                    service.list(null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ---------------------------------------------------------------
    // exportCsv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("exportCsv()")
    class ExportCsv {

        @Test
        @DisplayName("returns CSV bytes with header")
        void exportCsv_containsHeader() {
            when(repository.findAll()).thenReturn(List.of(specialty));

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).startsWith("ID,Nombre");
            assertThat(content).contains(NAME);
        }

        @Test
        @DisplayName("returns header only when repository is empty")
        void exportCsv_empty() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).startsWith("ID,Nombre");
            // only the header line
            assertThat(content.lines().filter(l -> !l.isBlank()).count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("escapes commas in CSV field")
        void exportCsv_escapesComma() {
            specialty.setName("Civil, Hidráulica");
            when(repository.findAll()).thenReturn(List.of(specialty));

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).contains("\"Civil, Hidráulica\"");
        }

        @Test
        @DisplayName("handles null description gracefully")
        void exportCsv_nullDescription() {
            specialty.setDescription(null);
            when(repository.findAll()).thenReturn(List.of(specialty));

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).doesNotContain("null");
        }
    }

    // ---------------------------------------------------------------
    // importFromCsv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("importFromCsv()")
    class ImportCsv {

        @Test
        @DisplayName("smoke test: valid CSV imports one row successfully")
        void importCsv_validFile_success() throws Exception {
            String csv = "nombre,descripcion\nEspecialidad A,Desc A\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Especialidad A")).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(
                    ProjectSpecialty.builder().id(2L).name("Especialidad A")
                            .description("Desc A").active(true)
                            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isZero();
        }

        @Test
        @DisplayName("returns error when 'nombre' column is missing in header")
        void importCsv_missingNameColumn() throws Exception {
            String csv = "descripcion\nDesc A\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            var result = service.importFromCsv(file);

            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("nombre");
        }

        @Test
        @DisplayName("returns empty result for empty file (no header)")
        void importCsv_emptyFile() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", new byte[0]);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isZero();
            assertThat(result.getSuccess()).isZero();
        }

        @Test
        @DisplayName("records error for duplicate row but continues processing")
        void importCsv_duplicateRow_countedAsFailed() throws Exception {
            String csv = "nombre,descripcion\nDuplicate,Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Duplicate")).thenReturn(true);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isZero();
            assertThat(result.getFailed()).isEqualTo(1);
        }

        @Test
        @DisplayName("records error for blank name in data row")
        void importCsv_blankNameRow() throws Exception {
            String csv = "nombre,descripcion\n ,Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            var result = service.importFromCsv(file);

            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("obligatorio");
        }

        @Test
        @DisplayName("blank lines in CSV are skipped")
        void importCsv_blankLinesSkipped() {
            String csv = "nombre,descripcion\n\nEspecialidad A,Desc\n\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Especialidad A")).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(specialty);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("quoted field with comma is parsed correctly")
        void importCsv_quotedField() {
            String csv = "nombre,descripcion\n\"Civil, Hidráulica\",Descripción\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Civil, Hidráulica")).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(specialty);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("double-quote inside quoted field is unescaped")
        void importCsv_doubleQuoteEscaping() {
            String csv = "nombre,descripcion\n\"Civil \"\"A\"\"\",Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Civil \"A\"")).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(specialty);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("row without description column uses null")
        void importCsv_missingDescriptionColumn() {
            String csv = "nombre\nEspecialidad A\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Especialidad A")).thenReturn(false);
            when(repository.save(any(ProjectSpecialty.class))).thenReturn(specialty);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }
    }

    // ---------------------------------------------------------------
    // escape (via exportCsv)
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("escape() via exportCsv()")
    class Escape {

        @Test
        @DisplayName("value with quote character is escaped")
        void escape_quoteInValue() {
            specialty.setName("Civil \"A\"");
            when(repository.findAll()).thenReturn(List.of(specialty));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Civil \"\"A\"\"\"");
        }

        @Test
        @DisplayName("value with newline is quoted")
        void escape_newlineInValue() {
            specialty.setName("Civil\nA");
            when(repository.findAll()).thenReturn(List.of(specialty));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Civil\nA\"");
        }

        @Test
        @DisplayName("null createdAt and updatedAt produce empty string in CSV")
        void formatDate_nullDates() {
            ProjectSpecialty noDate = ProjectSpecialty.builder()
                    .id(2L).name("Sin Fecha").active(true)
                    .createdAt(null).updatedAt(null).build();
            when(repository.findAll()).thenReturn(List.of(noDate));

            String content = new String(service.exportCsv());

            assertThat(content).contains("Sin Fecha");
        }
    }
}
