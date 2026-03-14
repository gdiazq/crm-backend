package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.entity.ProjectType;
import com.crm.mcsv_project.repository.ProjectTypeRepository;
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

@WebMvcTest(ProjectTypeSelectController.class)
@DisplayName("ProjectTypeSelectController Tests")
class ProjectTypeSelectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectTypeRepository projectTypeRepository;

    @Test
    @DisplayName("GET /select/project-types returns 200 with active items")
    void getAll_returnsActiveItems() throws Exception {
        ProjectType type = ProjectType.builder().id(1L).name("Obra Civil").active(true).build();
        when(projectTypeRepository.findByActiveTrue()).thenReturn(List.of(type));

        mockMvc.perform(get("/select/project-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Obra Civil")));
    }

    @Test
    @DisplayName("GET /select/project-types returns empty list when none active")
    void getAll_returnsEmptyList() throws Exception {
        when(projectTypeRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/select/project-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
