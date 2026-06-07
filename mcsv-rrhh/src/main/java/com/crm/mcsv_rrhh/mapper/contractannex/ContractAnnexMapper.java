package com.crm.mcsv_rrhh.mapper.contractannex;

import com.crm.mcsv_rrhh.dto.contractannex.UpdateContractAnnexRequest;
import com.crm.mcsv_rrhh.entity.contractannex.ContractAnnex;
import org.springframework.stereotype.Component;

@Component
public class ContractAnnexMapper {

    /** Aplica los campos no nulos de la actualización aprobada sobre el anexo existente. */
    public void applyUpdate(ContractAnnex annex, UpdateContractAnnexRequest proposed) {
        if (proposed.getAnnexTypeId() != null) annex.setAnnexTypeId(proposed.getAnnexTypeId());
        if (proposed.getDate() != null) annex.setDate(proposed.getDate());
        if (proposed.getDescription() != null) annex.setDescription(proposed.getDescription());
    }
}
