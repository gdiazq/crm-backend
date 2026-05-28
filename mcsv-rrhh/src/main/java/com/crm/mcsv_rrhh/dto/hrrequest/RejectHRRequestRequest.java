package com.crm.mcsv_rrhh.dto.hrrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectHRRequestRequest {

    @NotBlank
    private String rejectionDetail;
}
