package com.crm.mcsv_rrhh.mapper.contract;

import com.crm.mcsv_rrhh.dto.contract.CreateContractRequest;
import com.crm.mcsv_rrhh.dto.contract.UpdateContractRequest;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    /**
     * Arma un contrato nuevo a partir del request. El service resuelve y pasa los ids ya calculados:
     * tipo de contrato (puede derivarse a "Indefinido"), estado inicial del contrato y estado de la solicitud.
     */
    public Contract toEntity(CreateContractRequest request, Long contractTypeId,
                             Long contractStatusId, Long statusId) {
        return Contract.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .contractNumber(request.getContractNumber())
                .contractTypeId(contractTypeId)
                .contractStatusId(contractStatusId)
                .safetyGroupId(request.getSafetyGroupId())
                .contractDetail(request.getContractDetail())
                .baseSalary(request.getBaseSalary())
                .agreedSalary(request.getAgreedSalary())
                .companyId(request.getCompanyId())
                .zoneId(request.getZoneId())
                .jobTitleId(request.getJobTitleId())
                .siteId(request.getSiteId())
                .laborUnionId(request.getLaborUnionId())
                .costCenter(request.getCostCenter())
                .weeklyWorkHours(request.getWeeklyWorkHours())
                .workDays(request.getWorkDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .mealTypeId(request.getMealTypeId())
                .transportTypeId(request.getTransportTypeId())
                .statusId(statusId)
                .build();
    }

    /** Copia los campos de una solicitud de actualización aprobada sobre el contrato existente. */
    public void applyUpdate(Contract contract, UpdateContractRequest proposed) {
        contract.setName(proposed.getName());
        contract.setContractNumber(proposed.getContractNumber());
        contract.setContractTypeId(proposed.getContractTypeId());
        contract.setSafetyGroupId(proposed.getSafetyGroupId());
        contract.setContractDetail(proposed.getContractDetail());
        contract.setBaseSalary(proposed.getBaseSalary());
        contract.setAgreedSalary(proposed.getAgreedSalary());
        contract.setCompanyId(proposed.getCompanyId());
        contract.setZoneId(proposed.getZoneId());
        contract.setJobTitleId(proposed.getJobTitleId());
        contract.setSiteId(proposed.getSiteId());
        contract.setLaborUnionId(proposed.getLaborUnionId());
        contract.setCostCenter(proposed.getCostCenter());
        contract.setWeeklyWorkHours(proposed.getWeeklyWorkHours());
        contract.setWorkDays(proposed.getWorkDays());
        contract.setStartDate(proposed.getStartDate());
        contract.setEndDate(proposed.getEndDate());
        contract.setMealTypeId(proposed.getMealTypeId());
        contract.setTransportTypeId(proposed.getTransportTypeId());
    }
}
