package com.crm.mcsv_rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HRRequestDetailResponse {

    private Long id;
    private Long idModule;
    private String identification;
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;

    private CatalogItem requestType;
    private CatalogItem status;

    private Boolean requireApproval;

    private CatalogItem approver;
    private LocalDateTime approvalDate;

    private CatalogItem hhrrApprover;
    private LocalDateTime hhrrApprovalDate;

    private String rejectionDetail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
