package com.crm.mcsv_rrhh.service.contractannex;

import com.crm.mcsv_rrhh.dto.contractannex.ContractAnnexTypeResponse;

import java.util.List;

public interface ContractAnnexTypeService {

    List<ContractAnnexTypeResponse> selectActive();
}
