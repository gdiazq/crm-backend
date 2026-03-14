package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.dto.BulkImportResult;
import com.crm.mcsv_project.dto.PagedResponse;
import com.crm.mcsv_project.dto.ProjectSpecialtyRequest;
import com.crm.mcsv_project.dto.ProjectSpecialtyResponse;
import com.crm.mcsv_project.dto.UpdateProjectSpecialtyRequest;
import com.crm.mcsv_project.exception.DuplicateResourceException;
import com.crm.mcsv_project.exception.GlobalExceptionHandler;
import com.crm.mcsv_project.exception.ResourceNotFoundException;
import com.crm.mcsv_project.service.ProjectSpecialtyService;
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

import java.time.LocalDate;
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

@WebMvcTest(ProjectSpecialtyController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ProjectSpecialtyController Tests")
class ProjectSpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectSpecialtyService projectSpecialtyService;

    private ObjectMapper objectMapper;
    private ProjectSpecialtyResponse sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleResponse = ProjectSpecialtyResponse.builder()
                .id(1L)
                .name("Ingeniería Civil")
                .description("Especialidad en construcción")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ---------------------------------------------------------------
    // GET /project-specialty/paged
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-specialty/paged")
    class Paged {

        @Test
        @DisplayName("returns 200 with paged response for default params")
        void paged_defaultParams_returns200() throws Exception {
            PagedResponse<ProjectSpecialtyResponse> pagedResponse =
                    PagedResponse.<ProjectSpecialtyResponse>builder()
                            .content(List.of(sampleResponse))
                            .page(0).size(10).totalElements(1L).totalPages(1).last(true)
                            .total(1L).active(1L).build();

            when(projectSpecialtyService.list(
                    isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/project-specialty/paged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name", is("Ingeniería Civil")))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.active", is(1)));
        }

        @Test
        @DisplayName("passes search, active and date filters to service")
        void paged_withFilters_returns200() throws Exception {
            PagedResponse<ProjectSpecialtyResponse> pagedResponse =
                    PagedResponse.<ProjectSpecialtyResponse>builder()
                            .content(Collections.emptyList())
                            .page(0).size(5).totalElements(0L).totalPages(0).last(true)
                            .total(0L).active(0L).build();

            when(projectSpecialtyService.list(
                    eq("civil"), eq(true),
                    any(LocalDate.class), any(LocalDate.class),
                    isNull(), isNull(),
                    any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/project-specialty/paged")
                            .param("search", "civil")
                            .param("active", "true")
                            .param("createdFrom", "2024-01-01")
                            .param("createdTo", "2024-12-31")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sortBy", "name")
                            .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    // ---------------------------------------------------------------
    // GET /project-specialty/{id}
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-specialty/{id}")
    class GetById {

        @Test
        @DisplayName("returns 200 and response body for existing id")
        void getById_found_returns200() throws Exception {
            when(projectSpecialtyService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/project-specialty/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Ingeniería Civil")));
        }

        @Test
        @DisplayName("returns 404 when id not found")
        void getById_notFound_returns404() throws Exception {
            when(projectSpecialtyService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Especialidad no encontrada: 99"));

            mockMvc.perform(get("/project-specialty/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-specialty/create
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-specialty/create")
    class Create {

        @Test
        @DisplayName("returns 200 and created response for valid request")
        void create_valid_returns200() throws Exception {
            ProjectSpecialtyRequest req = new ProjectSpecialtyRequest("Ingeniería Civil", "Desc");
            when(projectSpecialtyService.create(any(ProjectSpecialtyRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(post("/project-specialty/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Ingeniería Civil")));
        }

        @Test
        @DisplayName("returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            ProjectSpecialtyRequest req = new ProjectSpecialtyRequest("", "Desc");

            mockMvc.perform(post("/project-specialty/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
        }

        @Test
        @DisplayName("returns 409 when name is duplicate")
        void create_duplicate_returns409() throws Exception {
            ProjectSpecialtyRequest req = new ProjectSpecialtyRequest("Ingeniería Civil", "Desc");
            when(projectSpecialtyService.create(any(ProjectSpecialtyRequest.class)))
                    .thenThrow(new DuplicateResourceException(
                            "Ya existe una especialidad con el nombre: Ingeniería Civil"));

            mockMvc.perform(post("/project-specialty/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status", is(409)));
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-specialty/update
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-specialty/update")
    class Update {

        @Test
        @DisplayName("returns 200 for valid update request")
        void update_valid_returns200() throws Exception {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(1L, "Nuevo Nombre", null);
            when(projectSpecialtyService.update(any(UpdateProjectSpecialtyRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put("/project-specialty/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)));
        }

        @Test
        @DisplayName("returns 400 when id is null")
        void update_nullId_returns400() throws Exception {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(null, "Nombre", null);

            mockMvc.perform(put("/project-specialty/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when entity does not exist")
        void update_notFound_returns404() throws Exception {
            UpdateProjectSpecialtyRequest req = new UpdateProjectSpecialtyRequest(99L, "X", null);
            when(projectSpecialtyService.update(any(UpdateProjectSpecialtyRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Especialidad no encontrada: 99"));

            mockMvc.perform(put("/project-specialty/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // PUT /project-specialty/{id}/status
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("PUT /project-specialty/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("returns 200 when toggling status")
        void updateStatus_returns200() throws Exception {
            doNothing().when(projectSpecialtyService).updateStatus(eq(1L), eq(false));

            mockMvc.perform(put("/project-specialty/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", false))))
                    .andExpect(status().isOk());

            verify(projectSpecialtyService).updateStatus(1L, false);
        }

        @Test
        @DisplayName("returns 404 when entity not found")
        void updateStatus_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Especialidad no encontrada: 99"))
                    .when(projectSpecialtyService).updateStatus(eq(99L), anyBoolean());

            mockMvc.perform(put("/project-specialty/99/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("active", true))))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------------------------------------------------------------
    // GET /project-specialty/export/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("GET /project-specialty/export/csv")
    class ExportCsv {

        @Test
        @DisplayName("returns 200 with CSV attachment header")
        void exportCsv_returns200() throws Exception {
            when(projectSpecialtyService.exportCsv())
                    .thenReturn("ID,Nombre\n1,Ingeniería Civil\n".getBytes());

            mockMvc.perform(get("/project-specialty/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            containsString("project-specialties.csv")));
        }
    }

    // ---------------------------------------------------------------
    // POST /project-specialty/import/csv
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("POST /project-specialty/import/csv")
    class ImportCsv {

        @Test
        @DisplayName("returns 200 with BulkImportResult for valid file")
        void importCsv_returns200() throws Exception {
            BulkImportResult result = BulkImportResult.builder()
                    .total(1).success(1).failed(0).errors(Collections.emptyList()).build();
            when(projectSpecialtyService.importFromCsv(any())).thenReturn(result);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.csv", "text/csv",
                    "nombre\nIngeniería Civil\n".getBytes());

            mockMvc.perform(multipart("/project-specialty/import/csv").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total", is(1)))
                    .andExpect(jsonPath("$.success", is(1)));
        }
    }
}
