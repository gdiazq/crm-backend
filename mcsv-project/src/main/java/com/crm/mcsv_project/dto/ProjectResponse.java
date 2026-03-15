package com.crm.mcsv_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private Integer costCenter;
    private String name;
    private String address;
    private String description;

    private CatalogItem type;
    private CatalogItem status;
    private CatalogItem specialty;

    private Long visitorId;
    private String visitorName;

    private Long supervisorId;
    private String supervisorName;

    private List<CatalogItem> companyRepresentatives;

    private LocalDate startDate;
    private LocalDate realStartDate;
    private LocalDate endDate;
    private LocalDate realEndDate;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
