package com.crm.mcsv_rrhh.service.company;

import com.crm.mcsv_rrhh.dto.company.CompanyResponse;

import java.util.List;

public interface CompanyService {

    List<CompanyResponse> selectAll();
}
