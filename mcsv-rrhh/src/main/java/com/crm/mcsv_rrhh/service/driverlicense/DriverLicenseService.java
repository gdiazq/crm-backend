package com.crm.mcsv_rrhh.service.driverlicense;

import com.crm.mcsv_rrhh.dto.driverlicense.DriverLicenseResponse;

import java.util.List;

public interface DriverLicenseService {

    List<DriverLicenseResponse> selectAll();
}
