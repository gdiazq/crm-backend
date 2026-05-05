package com.crm.mcsv_rrhh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendanceStatusRequest {

    @NotNull(message = "El ID del estado es obligatorio")
    private Long id;

    private String name;

    private String code;

    private String description;
}
