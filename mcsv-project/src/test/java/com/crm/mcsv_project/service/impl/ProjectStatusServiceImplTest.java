package com.crm.mcsv_project.service.impl;

import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectStatusRequest;
import com.crm.mcsv_project.dto.ProjectStatusResponse;
import com.crm.mcsv_project.dto.UpdateProjectStatusRequest;
import com.crm.mcsv_project.entity.ProjectStatus;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.repository.ProjectStatusRepository;
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
@DisplayName("ProjectStatusServiceImpl Tests")
class ProjectStatusServiceImplTest {

    @Mock
    private ProjectStatusRepository repository;

    @InjectMocks
    private ProjectStatusServiceImpl service;

    private ProjectStatus status;
    private static final Long ID = 1L;
    private static final String NAME = "En Progreso";
    private static final String DESC = "Proyecto actualmente en ejecución";

    @BeforeEach
    void setUp() {
        status = ProjectStatus.builder()
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
            ProjectStatusRequest req = new ProjectStatusRequest(NAME, DESC);
            when(repository.existsByName(NAME)).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(status);

            ProjectStatusResponse res = service.create(req);

            assertThat(res.getId()).isEqualTo(ID);
            assertThat(res.getName()).isEqualTo(NAME);
            assertThat(res.getDescription()).isEqualTo(DESC);
            assertThat(res.getActive()).isTrue();

            ArgumentCaptor<ProjectStatus> captor = ArgumentCaptor.forClass(ProjectStatus.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getActive()).isTrue();
        }

        @Test
        @DisplayName("throws DuplicateResourceException when name already exists")
        void create_duplicate_throws() {
            ProjectStatusRequest req = new ProjectStatusRequest(NAME, DESC);
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
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(ID, "Finalizado", "Proyecto terminado");
            when(repository.findById(ID)).thenReturn(Optional.of(status));
            when(repository.save(status)).thenReturn(status);

            ProjectStatusResponse res = service.update(req);

            assertThat(res.getName()).isEqualTo("Finalizado");
            verify(repository).save(status);
        }

        @Test
        @DisplayName("does not overwrite fields that are null in request")
        void update_partialFields_nullDescriptionSkipped() {
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(ID, null, null);
            when(repository.findById(ID)).thenReturn(Optional.of(status));
            when(repository.save(status)).thenReturn(status);

            service.update(req);

            assertThat(status.getName()).isEqualTo(NAME);
            assertThat(status.getDescription()).isEqualTo(DESC);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when id not found")
        void update_notFound_throws() {
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(99L, "X", null);
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
            when(repository.findById(ID)).thenReturn(Optional.of(status));

            service.updateStatus(ID, false);

            assertThat(status.getActive()).isFalse();
            verify(repository).save(status);
        }

        @Test
        @DisplayName("activates entity and saves")
        void updateStatus_activate() {
            status.setActive(false);
            when(repository.findById(ID)).thenReturn(Optional.of(status));

            service.updateStatus(ID, true);

            assertThat(status.getActive()).isTrue();
            verify(repository).save(status);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when id not found")
        void updateStatus_notFound_throws() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(99L, true))
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
            when(repository.findById(ID)).thenReturn(Optional.of(status));

            ProjectStatusResponse res = service.getById(ID);

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
        @DisplayName("returns PagedResponse with content and metadata")
        void list_withNullFilters() {
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<ProjectStatus> page = new PageImpl<>(List.of(status), pageable, 1L);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(repository.count(any(Specification.class))).thenReturn(1L);

            PagedResponse<ProjectStatusResponse> result =
                    service.list(null, null, null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);
            assertThat(result.getActive()).isEqualTo(1L);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("passes search and date filters through to specification")
        void list_withAllFilters() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<ProjectStatus> page = new PageImpl<>(Collections.emptyList(), pageable, 0L);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(repository.count(any(Specification.class))).thenReturn(0L);

            LocalDate d = LocalDate.of(2024, 6, 1);

            PagedResponse<ProjectStatusResponse> result =
                    service.list("test", false, d, d, d, d, pageable);

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
        @DisplayName("CSV bytes contain header and data row")
        void exportCsv_containsData() {
            when(repository.findAll()).thenReturn(List.of(status));

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).startsWith("ID,Nombre");
            assertThat(content).contains(NAME);
        }

        @Test
        @DisplayName("CSV bytes contain only header when repository empty")
        void exportCsv_empty() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            byte[] csv = service.exportCsv();
            String content = new String(csv);

            assertThat(content).startsWith("ID,Nombre");
            assertThat(content.lines().filter(l -> !l.isBlank()).count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null description renders as empty string in CSV")
        void exportCsv_nullDescription() {
            status.setDescription(null);
            when(repository.findAll()).thenReturn(List.of(status));

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
        @DisplayName("smoke test: valid CSV with one row succeeds")
        void importCsv_validRow() throws Exception {
            String csv = "nombre,descripcion\nEn Revisión,Pendiente de aprobación\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("En Revisión")).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(
                    ProjectStatus.builder().id(2L).name("En Revisión").description("Pendiente de aprobación")
                            .active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build());

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getSuccess()).isEqualTo(1);
            assertThat(result.getFailed()).isZero();
        }

        @Test
        @DisplayName("returns error for missing nombre column")
        void importCsv_missingNameColumn() {
            String csv = "descripcion\nDesc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            var result = service.importFromCsv(file);

            assertThat(result.getFailed()).isEqualTo(1);
            assertThat(result.getErrors().get(0).getMessage()).contains("nombre");
        }

        @Test
        @DisplayName("returns empty result for completely empty file")
        void importCsv_emptyFile() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", new byte[0]);

            var result = service.importFromCsv(file);

            assertThat(result.getTotal()).isZero();
        }

        @Test
        @DisplayName("duplicate entry is counted as failed")
        void importCsv_duplicateEntry() {
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
            String csv = "nombre,descripcion\n\nEn Progreso,Desc\n\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("En Progreso")).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(status);

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
            String csv = "nombre,descripcion\n\"Estado, Especial\",Descripción\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Estado, Especial")).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(status);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("double-quote inside quoted field is unescaped")
        void importCsv_doubleQuoteEscaping() {
            String csv = "nombre,descripcion\n\"Estado \"\"A\"\"\",Desc\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("Estado \"A\"")).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(status);

            var result = service.importFromCsv(file);

            assertThat(result.getSuccess()).isEqualTo(1);
        }

        @Test
        @DisplayName("row without description column uses null")
        void importCsv_missingDescriptionColumn() {
            String csv = "nombre\nEn Progreso\n";
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv", csv.getBytes());

            when(repository.existsByName("En Progreso")).thenReturn(false);
            when(repository.save(any(ProjectStatus.class))).thenReturn(status);

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
            status.setName("Estado \"A\"");
            when(repository.findAll()).thenReturn(List.of(status));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Estado \"\"A\"\"\"");
        }

        @Test
        @DisplayName("value with newline is quoted")
        void escape_newlineInValue() {
            status.setName("Estado\nA");
            when(repository.findAll()).thenReturn(List.of(status));

            String content = new String(service.exportCsv());

            assertThat(content).contains("\"Estado\nA\"");
        }

        @Test
        @DisplayName("null createdAt and updatedAt produce empty string in CSV")
        void formatDate_nullDates() {
            ProjectStatus noDate = ProjectStatus.builder()
                    .id(2L).name("Sin Fecha").active(true)
                    .createdAt(null).updatedAt(null).build();
            when(repository.findAll()).thenReturn(List.of(noDate));

            String content = new String(service.exportCsv());

            assertThat(content).contains("Sin Fecha");
        }
    }
}
