package com.crm.mcsv_rrhh.service.paymentmethod;

import com.crm.mcsv_rrhh.dto.paymentmethod.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {

    List<PaymentMethodResponse> selectAll();
}
