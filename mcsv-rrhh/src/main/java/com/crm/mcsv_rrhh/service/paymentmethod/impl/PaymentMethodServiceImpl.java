package com.crm.mcsv_rrhh.service.paymentmethod.impl;

import com.crm.mcsv_rrhh.dto.paymentmethod.PaymentMethodResponse;
import com.crm.mcsv_rrhh.entity.paymentmethod.PaymentMethod;
import com.crm.mcsv_rrhh.repository.paymentmethod.PaymentMethodRepository;
import com.crm.mcsv_rrhh.service.paymentmethod.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repository;

    @Override
    public List<PaymentMethodResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> PaymentMethodResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
