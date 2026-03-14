package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.entity.ProjectStatus;
import com.crm.mcsv_project.repository.ProjectStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectStatusSelectController.class)
@DisplayName("ProjectStatusSelectController Tests")
class ProjectStatusSelectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectStatusRepository projectStatusRepository;

    @Test
    @DisplayName("GET /select/project-statuses returns 200 with active items")
    void getAll_returnsActiveItems() throws Exception {
        ProjectStatus status = ProjectStatus.builder().id(1L).name("En Progreso").active(true).build();
        when(projectStatusRepository.findByActiveTrue()).thenReturn(List.of(status));

        mockMvc.perform(get("/select/project-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("En Progreso")));
    }

    @Test
    @DisplayName("GET /select/project-statuses returns empty list when none active")
    void getAll_returnsEmptyList() throws Exception {
        when(projectStatusRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/select/project-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
