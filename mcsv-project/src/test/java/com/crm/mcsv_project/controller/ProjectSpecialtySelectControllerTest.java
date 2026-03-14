package com.crm.mcsv_project.controller;

import com.crm.mcsv_project.entity.ProjectSpecialty;
import com.crm.mcsv_project.repository.ProjectSpecialtyRepository;
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

@WebMvcTest(ProjectSpecialtySelectController.class)
@DisplayName("ProjectSpecialtySelectController Tests")
class ProjectSpecialtySelectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectSpecialtyRepository projectSpecialtyRepository;

    @Test
    @DisplayName("GET /select/project-specialties returns 200 with active items")
    void getAll_returnsActiveItems() throws Exception {
        ProjectSpecialty specialty = ProjectSpecialty.builder().id(1L).name("Backend").active(true).build();
        when(projectSpecialtyRepository.findByActiveTrue()).thenReturn(List.of(specialty));

        mockMvc.perform(get("/select/project-specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Backend")));
    }

    @Test
    @DisplayName("GET /select/project-specialties returns empty list when none active")
    void getAll_returnsEmptyList() throws Exception {
        when(projectSpecialtyRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/select/project-specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
