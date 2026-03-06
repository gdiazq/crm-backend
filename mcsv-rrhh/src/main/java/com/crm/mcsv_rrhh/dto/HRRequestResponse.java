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
public class HRRequestResponse {

    private Long id;
    private Long idModule;
    private String identification;
    private String firstName;
    private String paternalLastName;
    private String maternalLastName;

    private Long requestTypeId;
    private String requestTypeName;

    private Long statusId;
    private String statusName;

    private Long approverId;
    private String approverFullName;
    private LocalDateTime approvalDate;

    private Long hhrrApproverId;
    private String hhrrApproverFullName;
    private LocalDateTime hhrrApprovalDate;

    private String action;

    private String rejectionDetail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
