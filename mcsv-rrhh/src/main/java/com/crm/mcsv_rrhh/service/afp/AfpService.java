package com.crm.mcsv_rrhh.service.afp;

import com.crm.mcsv_rrhh.dto.afp.AfpResponse;

import java.util.List;

public interface AfpService {

    List<AfpResponse> selectAll();
}
