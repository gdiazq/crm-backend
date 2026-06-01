package com.crm.mcsv_rrhh.service.bank;

import com.crm.mcsv_rrhh.dto.bank.BankResponse;

import java.util.List;

public interface BankService {

    List<BankResponse> selectAll();
}
