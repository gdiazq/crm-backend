package com.crm.mcsv_rrhh.service.site;

import com.crm.mcsv_rrhh.dto.site.SiteResponse;

import java.util.List;

public interface SiteService {

    List<SiteResponse> selectAll();
}
