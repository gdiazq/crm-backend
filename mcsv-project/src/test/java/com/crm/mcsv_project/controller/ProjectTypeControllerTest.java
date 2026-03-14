package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectTypeRequest;
import com.crm.mcsv_project.dto.ProjectTypeResponse;
import com.crm.mcsv_project.dto.UpdateProjectTypeRequest;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.GlobalExceptionHandler;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.service.ProjectTypeService;
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

@WebMvcTest(ProjectTypeController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ProjectTypeController Tests")
class ProjectTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectTypeService projectTypeService;

    private ObjectMapper objectMapper;
    private ProjectTypeResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleResponse = ProjectTypeResponse.builder()
                .id(1L)
                .name("Obra Civil")
                .description("Proyectos de construcción")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ---------------------------------------------------------------
    // GET /project-type/paged
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-type/paged")
    class Paged {

        @Test
        @DisplayName("returns 200 with paged content")
        void paged_returns200() throws Exception {
            PagedResponse<ProjectTypeResponse> pagedResponse =
                    PagedResponse.<ProjectTypeResponse>builder()
                            .content(List.of(sampleResponse))
                            .page(0).size(10).totalElements(1L).totalPages(1).last(true)
                            .total(1L).active(1L).build();

            when(projectTypeService.list(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/project-type/paged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Obra Civil")))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        @DisplayName("ascending sort direction is handled")
        void paged_ascSort() throws Exception {
            PagedResponse<ProjectTypeResponse> empty =
                    PagedResponse.<ProjectTypeResponse>builder()
                            .content(Collections.emptyList())
                            .page(0).size(10).totalElements(0L).totalPages(0).last(true)
                            .total(0L).active(0L).build();

            when(projectTypeService.list(
                    any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                    .thenReturn(empty);

            mockMvc.perform(get("/project-type/paged")
                            .param("sortBy", "name")
                            .param("sortDir", "asc"))
                    .andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------
    // GET /project-type/{id}
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-type/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 for existing id")
        void getById_found_returns200() throws Exception {
            when(projectTypeService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/project-type/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Obra Civil")));
        }

        @Test
        @DisplayName("returns 404 for unknown id")
        void getById_notFound_returns404() throws Exception {
            when(projectTypeService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Tipo de proyecto no encontrado: 99"));

            mockMvc.perform(get("/project-type/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-type/create
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-type/create")
    class Create {

        @Test
        @DisplayName("returns 200 for valid request")
        void create_valid_returns200() throws Exception {
            ProjectTypeRequest req = new ProjectTypeRequest("Obra Civil", "Desc");
            when(projectTypeService.create(any(ProjectTypeRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/project-type/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            ProjectTypeRequest req = new ProjectTypeRequest("", null);

            mockMvc.perform(post("/project-type/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
        }

        @Test
        @DisplayName("returns 409 for duplicate name")
        void create_duplicate_returns409() throws Exception {
            ProjectTypeRequest req = new ProjectTypeRequest("Obra Civil", null);
            when(projectTypeService.create(any(ProjectTypeRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Ya existe un tipo con el nombre: Obra Civil"));

            mockMvc.perform(post("/project-type/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)));
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-type/update
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-type/update")
    class Update {

        @Test
        @DisplayName("returns 200 for valid update")
        void update_valid_returns200() throws Exception {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(1L, "Nuevo Tipo", null);
            when(projectTypeService.update(any(UpdateProjectTypeRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put("/project-type/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 400 when id is null")
        void update_nullId_returns400() throws Exception {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(null, "Nombre", null);

            mockMvc.perform(put("/project-type/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when entity not found")
        void update_notFound_returns404() throws Exception {
            UpdateProjectTypeRequest req = new UpdateProjectTypeRequest(99L, "X", null);
            when(projectTypeService.update(any(UpdateProjectTypeRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Tipo de proyecto no encontrado: 99"));

            mockMvc.perform(put("/project-type/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-type/{id}/status
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-type/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("returns 200 when toggling status")
        void updateStatus_returns200() throws Exception {
            doNothing().when(projectTypeService).updateStatus(eq(1L), eq(false));

            mockMvc.perform(put("/project-type/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", false))))
                    .andExpect(status().isOk());

            verify(projectTypeService).updateStatus(1L, false);
        }

        @Test
        @DisplayName("returns 404 when entity not found")
        void updateStatus_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Tipo de proyecto no encontrado: 99"))
                    .when(projectTypeService).updateStatus(eq(99L), anyBoolean());

            mockMvc.perform(put("/project-type/99/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", true))))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // GET /project-type/export/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-type/export/csv")
    class ExportCsv {

        @Test
        @DisplayName("returns 200 with attachment header")
        void exportCsv_returns200() throws Exception {
            when(projectTypeService.exportCsv())
                    .thenReturn("ID,Nombre\n1,Obra Civil\n".getBytes());

            mockMvc.perform(get("/project-type/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            containsString("project-types.csv")));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-type/import/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-type/import/csv")
    class ImportCsv {

        @Test
        @DisplayName("returns 200 with BulkImportResult")
        void importCsv_returns200() throws Exception {
            BulkImportResult result = BulkImportResult.builder()
                    .total(1).success(1).failed(0).errors(Collections.emptyList()).build();
            when(projectTypeService.importFromCsv(any())).thenReturn(result);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "types.csv", "text/csv",
                    "nombre\nObra Civil\n".getBytes());

            mockMvc.perform(multipart("/project-type/import/csv").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total", is(1)))
                    .andExpect(jsonPath("$.success", is(1)));
        }
    }
}
