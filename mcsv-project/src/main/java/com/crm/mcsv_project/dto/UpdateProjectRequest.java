package com.crm.mcsv_project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {

    @NotNull(message = "El id es obligatorio")
    @Schema(description = "ID del proyecto")
    private Long id;

    @Schema(description = "Código de centro de costo (único)", example = "1001")
    private Integer costCenter;

    @Schema(description = "Nombre del proyecto")
    private String name;

    @Schema(description = "Dirección del proyecto")
    private String address;

    @Schema(description = "Descripción del proyecto")
    private String description;

    @Schema(description = "ID del tipo de proyecto")
    private Long typeId;

    @Schema(description = "ID del estado del proyecto")
    private Long statusId;

    @Schema(description = "ID de la especialidad del proyecto")
    private Long specialtyId;

    @Schema(description = "ID del visitador (mcsv-user)")
    private Long visitorId;

    @Schema(description = "ID del supervisor (mcsv-rrhh)")
    private Long supervisorId;

    @Schema(description = "IDs de representantes de empresa (mcsv-rrhh)")
    private List<Long> companyRepresentativeIds;

    @Schema(description = "Fecha de inicio planificada")
    private LocalDate startDate;

    @Schema(description = "Fecha de inicio real")
    private LocalDate realStartDate;

    @Schema(description = "Fecha de fin planificada")
    private LocalDate endDate;

    @Schema(description = "Fecha de fin real")
    private LocalDate realEndDate;
}
