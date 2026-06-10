package com.crm.mcsv_rrhh.mapper.contractannex;

import com.crm.common.dto.FileMetadataResponse;
import com.crm.mcsv_rrhh.dto.contractannex.ContractAnnexResponse;
import com.crm.mcsv_rrhh.dto.contractannex.UpdateContractAnnexRequest;
import com.crm.mcsv_rrhh.entity.contractannex.ContractAnnex;
import com.crm.mcsv_rrhh.entity.contractannex.ContractAnnexType;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.util.employee.EmployeeNames;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractAnnexMapper {

    public ContractAnnexResponse toResponse(ContractAnnex e, List<FileMetadataResponse> documents,
                                            Long requestId, String statusName) {
        Employee emp = e.getEmployee();
        ContractAnnexType annexType = e.getAnnexType();
        return ContractAnnexResponse.builder()
                .id(e.getId())
                .status(statusName)
                .employeeId(e.getEmployeeId())
                .employeeFullName(EmployeeNames.full(emp))
                .employeeIdentification(emp != null ? emp.getIdentification() : null)
                .contractId(e.getContractId())
                .annexTypeId(e.getAnnexTypeId())
                .annexTypeName(annexType != null ? annexType.getName() : null)
                .requireApproval(annexType != null ? annexType.getRequireApproval() : null)
                .date(e.getDate())
                .description(e.getDescription())
                .documents(documents)
                .hrRequestId(requestId)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    /** Aplica los campos no nulos de la actualización aprobada sobre el anexo existente. */
    public void applyUpdate(ContractAnnex annex, UpdateContractAnnexRequest proposed) {
        if (proposed.getAnnexTypeId() != null) annex.setAnnexTypeId(proposed.getAnnexTypeId());
        if (proposed.getDate() != null) annex.setDate(proposed.getDate());
        if (proposed.getDescription() != null) annex.setDescription(proposed.getDescription());
    }
}
