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
public class UpdateContractAnnexRequest {

    @NotNull(message = "El ID del anexo es obligatorio")
    @Schema(description = "ID del anexo a actualizar")
    private Long id;

    @Schema(description = "ID del tipo de anexo")
    private Long annexTypeId;

    @Schema(description = "Fecha de vigencia")
    private LocalDate date;

    @Schema(description = "Descripción del anexo")
    private String description;
}
