package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectStatusRequest;
import com.crm.mcsv_project.dto.ProjectStatusResponse;
import com.crm.mcsv_project.dto.UpdateProjectStatusRequest;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.GlobalExceptionHandler;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.service.ProjectStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectStatusController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ProjectStatusController Tests")
class ProjectStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectStatusService projectStatusService;

    private ObjectMapper objectMapper;
    private ProjectStatusResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleResponse = ProjectStatusResponse.builder()
                .id(1L)
                .name("En Progreso")
                .description("Proyecto activo")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ---------------------------------------------------------------
    // GET /project-status/paged
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-status/paged")
    class Paged {

        @Test
        @DisplayName("returns 200 with paged response")
        void paged_returns200() throws Exception {
            PagedResponse<ProjectStatusResponse> pagedResponse =
                    PagedResponse.<ProjectStatusResponse>builder()
                            .content(List.of(sampleResponse))
                            .page(0).size(10).totalElements(1L).totalPages(1).last(true)
                            .total(1L).active(1L).build();

            when(projectStatusService.list(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/project-status/paged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("En Progreso")))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        @DisplayName("descending sort direction is handled")
        void paged_descendingSort() throws Exception {
            PagedResponse<ProjectStatusResponse> empty =
                    PagedResponse.<ProjectStatusResponse>builder()
                            .content(Collections.emptyList())
                            .page(0).size(10).totalElements(0L).totalPages(0).last(true)
                            .total(0L).active(0L).build();

            when(projectStatusService.list(
                    any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(empty);

            mockMvc.perform(get("/project-status/paged").param("sortDir", "desc"))
                    .andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------
    // GET /project-status/{id}
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-status/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 for existing id")
        void getById_found_returns200() throws Exception {
            when(projectStatusService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/project-status/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("En Progreso")));
        }

        @Test
        @DisplayName("returns 404 for unknown id")
        void getById_notFound_returns404() throws Exception {
            when(projectStatusService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Estado de proyecto no encontrado: 99"));

            mockMvc.perform(get("/project-status/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-status/create
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-status/create")
    class Create {

        @Test
        @DisplayName("returns 200 for valid request")
        void create_valid_returns200() throws Exception {
            ProjectStatusRequest req = new ProjectStatusRequest("En Progreso", "Activo");
            when(projectStatusService.create(any(ProjectStatusRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/project-status/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            ProjectStatusRequest req = new ProjectStatusRequest("", null);

            mockMvc.perform(post("/project-status/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
        }

        @Test
        @DisplayName("returns 409 for duplicate name")
        void create_duplicate_returns409() throws Exception {
            ProjectStatusRequest req = new ProjectStatusRequest("En Progreso", null);
            when(projectStatusService.create(any(ProjectStatusRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Ya existe un estado con el nombre: En Progreso"));

            mockMvc.perform(post("/project-status/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)));
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-status/update
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-status/update")
    class Update {

        @Test
        @DisplayName("returns 200 for valid update")
        void update_valid_returns200() throws Exception {
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(1L, "Finalizado", null);
            when(projectStatusService.update(any(UpdateProjectStatusRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put("/project-status/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 400 when id is null")
        void update_nullId_returns400() throws Exception {
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(null, "Nombre", null);

            mockMvc.perform(put("/project-status/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when entity not found")
        void update_notFound_returns404() throws Exception {
            UpdateProjectStatusRequest req = new UpdateProjectStatusRequest(99L, "X", null);
            when(projectStatusService.update(any(UpdateProjectStatusRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Estado de proyecto no encontrado: 99"));

            mockMvc.perform(put("/project-status/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-status/{id}/status
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-status/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("returns 200 when toggling status")
        void updateStatus_returns200() throws Exception {
            doNothing().when(projectStatusService).updateStatus(eq(1L), eq(true));

            mockMvc.perform(put("/project-status/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", true))))
                    .andExpect(status().isOk());

            verify(projectStatusService).updateStatus(1L, true);
        }

        @Test
        @DisplayName("returns 404 when entity not found")
        void updateStatus_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Estado de proyecto no encontrado: 99"))
                    .when(projectStatusService).updateStatus(eq(99L), anyBoolean());

            mockMvc.perform(put("/project-status/99/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", false))))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // GET /project-status/export/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-status/export/csv")
    class ExportCsv {

        @Test
        @DisplayName("returns 200 with attachment header")
        void exportCsv_returns200() throws Exception {
            when(projectStatusService.exportCsv())
                    .thenReturn("ID,Nombre\n1,En Progreso\n".getBytes());

            mockMvc.perform(get("/project-status/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            containsString("project-statuses.csv")));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-status/import/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-status/import/csv")
    class ImportCsv {

        @Test
        @DisplayName("returns 200 with BulkImportResult")
        void importCsv_returns200() throws Exception {
            BulkImportResult result = BulkImportResult.builder()
                    .total(2).success(2).failed(0).errors(Collections.emptyList()).build();
            when(projectStatusService.importFromCsv(any())).thenReturn(result);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "statuses.csv", "text/csv",
                    "nombre\nEn Progreso\nFinalizado\n".getBytes());

            mockMvc.perform(multipart("/project-status/import/csv").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total", is(2)))
                    .andExpect(jsonPath("$.success", is(2)));
        }
    }
}
