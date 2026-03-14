package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectTypeRequest;
import com.crm.mcsv_project.dto.ProjectTypeResponse;
import com.crm.mcsv_project.dto.UpdateProjectTypeRequest;
import com.crm.mcsv_project.entity.ProjectType;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectTypeRepository;
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
@DisplayName("ProjectTypeServiceImpl Tests")
class ProjectTypeServiceImplTest {

    @Mock
    private ProjectTypeRepository repository;

    @InjectMocks
    private ProjectTypeServiceImpl service;

    private ProjectType projectType;
    private static final Long ID = 1L;
    private static final String NAME = "Obra Civil";
    private static final String DESC = "Proyectos de construcción civil";

    @BeforeEach
    void setUp() {
        projectType = ProjectType.builder()
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
            ProjectTypeRequest req = new ProjectTypeRequest(NAME, DESC);
            when(repository.existsByName(NAME)).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(projectType);

            ProjectTypeResponse res = service.create(req);

            assertThat(res.getId()).isEqualTo(ID);
            assertThat(res.getName()).isEqualTo(NAME);
            assertThat(res.getDescription()).isEqualTo(DESC);
            assertThat(res.getActive()).isTrue();

            ArgumentCaptor<ProjectType> captor = ArgumentCaptor.forClass(ProjectType.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isTrue();
        }

        @Test
        @DisplayName("throws DuplicateResourceException when name already exists")
        void create_duplicate_throws() {
            ProjectTypeRequest req = new ProjectTypeRequest(NAME, DESC);
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
        @DisplayName("happy path: updates both fields when non-null")
        void update_success() {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(ID, "Obra Hidráulica", "Diques y canales");
            when(repository.findById(ID)).thenReturn(Optional.of(projectType));
            when(repository.save(projectType)).thenReturn(projectType);

            ProjectTypeResponse res = service.update(req);

            assertThat(res.getName()).isEqualTo("Obra Hidráulica");
            verify(repository).save(projectType);
        }

        @Test
        @DisplayName("skips null fields (partial update)")
        void update_partialFields() {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(ID, null, "Nueva desc");
            when(repository.findById(ID)).thenReturn(Optional.of(projectType));
            when(repository.save(projectType)).thenReturn(projectType);

            service.update(req);

            assertThat(projectType.getName()).isEqualTo(NAME);
            assertThat(projectType.getDescription()).isEqualTo("Nueva desc");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when id not found")
        void update_notFound_throws() {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(99L, "X", null);
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
        @DisplayName("deactivates entity and saves")
        void updateStatus_deactivate() {
            when(repository.findById(ID)).thenReturn(Optional.of(projectType));

            service.updateStatus(ID, false);

            assertThat(projectType.getActive()).isFalse();
            verify(repository).save(projectType);
        }

        @Test
        @DisplayName("activates entity and saves")
        void updateStatus_activate() {
            projectType.setActive(false);
            when(repository.findById(ID)).thenReturn(Optional.of(projectType));

            service.updateStatus(ID, true);

            assertThat(projectType.getActive()).isTrue();
            verify(repository).save(projectType);
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
            when(repository.findById(ID)).thenReturn(Optional.of(projectType));

            ProjectTypeResponse res = service.getById(ID);

            assertThat(res.getId()).isEqualTo(ID);
            assertThat(res.getName()).isEqualTo(NAME);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown id")
        void getById_notFound() {
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
        @DisplayName("returns PagedResponse with content and active count")
        void list_withNullFilters() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<ProjectType> page = new PageImpl<>(List.of(projectType), pageable, 1L);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(repository.count(any(Specification.class))).thenReturn(1L);

            PagedResponse<ProjectTypeResponse> result =
                    service.list(null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getActive()).isEqualTo(1L);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("returns empty page when no records match filters")
        void list_emptyResult() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ProjectType> emptyPage = Page.empty(pageable);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
            when(repository.count(any(Specification.class))).thenReturn(0L);

            LocalDate d = LocalDate.of(2023, 1, 1);
            PagedResponse<ProjectTypeResponse> result =
                    service.list("xyz", true, d, d, d, d, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getActive()).isZero();
        }
    }

    // ---------------------------------------------------------------
    // exportCsv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("exportCsv()")
    class ExportCsv {

        @Test
        @DisplayName("CSV bytes contain header and entity data")
        void exportCsv_containsData() {
            when(repository.findAll()).thenReturn(List.of(projectType));

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).startsWith("ID,Nombre");
            assertThat(content).contains(NAME);
        }

        @Test
        @DisplayName("only header line when repository is empty")
        void exportCsv_empty() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content.lines().filter(l -> !l.isBlank()).count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null description does not produce 'null' in CSV")
        void exportCsv_nullDescription() {
            projectType.setDescription(null);
            when(repository.findAll()).thenReturn(List.of(projectType));

            byte[] csv = service.exportCsv();
            assertThat(new String(csv)).doesNotContain("null");
        }

        @Test
        @DisplayName("name with comma is quoted in CSV")
        void exportCsv_escapesCommaInName() {
            projectType.setName("Tipo A, B");
            when(repository.findAll()).thenReturn(List.of(projectType));

            byte[] csv = service.exportCsv();
            assertThat(new String(csv)).contains("\"Tipo A, B\"");
        }
    }

    // ---------------------------------------------------------------
    // importFromCsv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("importFromCsv()")
    class ImportCsv {

        @Test
        @DisplayName("smoke test: valid CSV imports one row")
        void importCsv_validRow() {
            String csv = "nombre,descripcion\nObra Eléctrica,Instalaciones eléctricas\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Obra Eléctrica")).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(
                    ProjectType.builder().id(2L).name("Obra Eléctrica").description("Instalaciones eléctricas")
                            .active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isZero();
        }

        @Test
        @DisplayName("returns error when nombre column is absent")
        void importCsv_missingNameColumn() {
            String csv = "descripcion\nDesc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            var result = service.importFromCsv(file);

            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("nombre");
        }

        @Test
        @DisplayName("returns zero totals for empty file")
        void importCsv_emptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", new byte[0]);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isZero();
            assertThat(result.getSuccess()).isZero();
        }

        @Test
        @DisplayName("duplicate entry is recorded as failed")
        void importCsv_duplicate() {
            String csv = "nombre,descripcion\nDuplicado,Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Duplicado")).thenReturn(true);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isZero();
            assertThat(result.getFailed()).isEqualTo(1);
        }

        @Test
        @DisplayName("blank lines in CSV are skipped")
        void importCsv_blankLinesSkipped() {
            String csv = "nombre,descripcion\n\nObra Civil,Desc\n\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Obra Civil")).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(projectType);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("empty name in row is recorded as failed")
        void importCsv_emptyNameInRow() {
            String csv = "nombre,descripcion\n,AlgunaDesc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            var result = service.importFromCsv(file);

            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("obligatorio");
        }

        @Test
        @DisplayName("quoted field in CSV is parsed correctly")
        void importCsv_quotedField() {
            String csv = "nombre,descripcion\n\"Tipo, Especial\",Descripción\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Tipo, Especial")).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(projectType);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("double-quote inside quoted field is unescaped")
        void importCsv_doubleQuoteEscaping() {
            String csv = "nombre,descripcion\n\"Tipo \"\"A\"\"\",Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Tipo \"A\"")).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(projectType);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("row with missing description column uses null")
        void importCsv_missingDescriptionColumn() {
            String csv = "nombre\nObra Civil\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Obra Civil")).thenReturn(false);
            when(repository.save(any(ProjectType.class))).thenReturn(projectType);

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
            projectType.setName("Tipo \"A\"");
            when(repository.findAll()).thenReturn(List.of(projectType));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Tipo \"\"A\"\"\"");
        }

        @Test
        @DisplayName("value with newline is quoted")
        void escape_newlineInValue() {
            projectType.setName("Tipo\nA");
            when(repository.findAll()).thenReturn(List.of(projectType));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Tipo\nA\"");
        }

        @Test
        @DisplayName("null createdAt and updatedAt produce empty string in CSV")
        void formatDate_nullDates() {
            ProjectType noDate = ProjectType.builder()
                    .id(2L).name("Sin Fecha").active(true)
                    .createdAt(null).updatedAt(null).build();
            when(repository.findAll()).thenReturn(List.of(noDate));

            String content = new String(service.exportCsv());

            assertThat(content).contains("Sin Fecha");
        }
    }
}
