package com.crm.mcsv_rrhh.mapper.contract;

import com.crm.mcsv_rrhh.dto.contract.UpdateContractRequest;
import com.crm.mcsv_rrhh.entity.contract.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

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
