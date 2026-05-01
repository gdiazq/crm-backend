package com.crm.mcsv_rrhh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeLeaveRequest {

    @NotNull(message = "El ID del permiso es obligatorio")
    @Schema(description = "ID del permiso a actualizar")
    private Long id;

    @Schema(description = "ID del tipo de permiso")
    private Long leaveTypeId;

    @Schema(description = "Fecha de inicio del permiso")
    private LocalDate startDate;

    @Schema(description = "Fecha de término del permiso")
    private LocalDate endDate;

    @Schema(description = "Indica si el permiso corresponde a medio día")
    private Boolean halfDay;

    @Schema(description = "Motivo o detalle del permiso")
    private String reason;
}
