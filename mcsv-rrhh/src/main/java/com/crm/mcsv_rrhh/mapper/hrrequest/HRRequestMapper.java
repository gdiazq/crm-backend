package com.crm.mcsv_rrhh.mapper.hrrequest;

import com.crm.mcsv_rrhh.dto.CatalogItem;
import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestDetailResponse;
import com.crm.mcsv_rrhh.dto.hrrequest.HRRequestResponse;
import com.crm.mcsv_rrhh.entity.employee.Employee;
import com.crm.mcsv_rrhh.entity.employee.EmployeeStatus;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequest;
import com.crm.mcsv_rrhh.entity.hrrequest.HRRequestType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;


@Component
public class HRRequestMapper {

    public HRRequest toEntity(HRRequestType type, Long statusId, Long employeeId, String action,
                              String proposedData, Consumer<HRRequest.HRRequestBuilder> link) {
        HRRequest.HRRequestBuilder builder = HRRequest.builder()
                .requestTypeId(type.getId())
                .statusId(statusId)
                .requireApproval(type.getRequireApproval())
                .idModule(employeeId)
                .action(action)
                .proposedData(proposedData);
        if (link != null) link.accept(builder);
        return builder.build();
    }

    /**
     * Arma el detalle de una HRRequest. El service resuelve y pasa el tipo, el estado, el empleado
     * y los nombres de los aprobadores; aquí solo se construye el DTO (incluidos los CatalogItem).
     */
    public HRRequestDetailResponse toDetailResponse(HRRequest hr, HRRequestType type, EmployeeStatus status,
                                                    Employee employee, String approverName, String hhrrApproverName) {
        HRRequestDetailResponse.HRRequestDetailResponseBuilder builder = HRRequestDetailResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestType(type != null ? new CatalogItem(type.getId(), type.getName()) : null)
                .status(status != null ? new CatalogItem(status.getId(), status.getName()) : null)
                .requireApproval(hr.getRequireApproval())
                .action(hr.getAction())
                .approvalDate(hr.getApprovalDate())
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        if (employee != null) {
            builder.identification(employee.getIdentification())
                   .firstName(employee.getFirstName())
                   .paternalLastName(employee.getPaternalLastName())
                   .maternalLastName(employee.getMaternalLastName());
        }

        if (hr.getApproverId() != null) {
            builder.approver(new CatalogItem(hr.getApproverId(), approverName));
        }
        if (hr.getHhrrApproverId() != null) {
            builder.hhrrApprover(new CatalogItem(hr.getHhrrApproverId(), hhrrApproverName));
        }

        return builder.build();
    }

    /**
     * Arma el resumen de una HRRequest a partir de los datos ya resueltos por el service
     * (nombres de tipo/estado, empleado y nombres de aprobadores). Sin acceso a datos.
     */
    public HRRequestResponse toResponse(HRRequest hr, String typeName, String statusName,
                                        Employee employee, String approverName, String hhrrApproverName) {
        HRRequestResponse.HRRequestResponseBuilder builder = HRRequestResponse.builder()
                .id(hr.getId())
                .idModule(hr.getIdModule())
                .requestTypeId(hr.getRequestTypeId())
                .requestTypeName(typeName)
                .statusId(hr.getStatusId())
                .statusName(statusName)
                .action(hr.getAction())
                .approverId(hr.getApproverId())
                .approverFullName(approverName)
                .approvalDate(hr.getApprovalDate())
                .hhrrApproverId(hr.getHhrrApproverId())
                .hhrrApproverFullName(hhrrApproverName)
                .hhrrApprovalDate(hr.getHhrrApprovalDate())
                .rejectionDetail(hr.getRejectionDetail())
                .createdAt(hr.getCreatedAt())
                .updatedAt(hr.getUpdatedAt());

        if (employee != null) {
            builder.identification(employee.getIdentification())
                   .firstName(employee.getFirstName())
                   .paternalLastName(employee.getPaternalLastName())
                   .maternalLastName(employee.getMaternalLastName());
        }

        return builder.build();
    }

    /**
     * Variante para el listado: el service pre-carga en lote los catálogos, empleados y
     * aprobadores; aquí solo se resuelven contra esos mapas y se delega al método base.
     */
    public HRRequestResponse toResponse(HRRequest hr,
                                        Map<Long, String> typeNames,
                                        Map<Long, String> statusNames,
                                        Map<Long, Employee> employees,
                                        Map<Long, String> approverNames) {
        return toResponse(hr,
                typeNames.get(hr.getRequestTypeId()),
                statusNames.get(hr.getStatusId()),
                employees.get(hr.getIdModule()),
                hr.getApproverId() != null ? approverNames.get(hr.getApproverId()) : null,
                hr.getHhrrApproverId() != null ? approverNames.get(hr.getHhrrApproverId()) : null);
    }
}
