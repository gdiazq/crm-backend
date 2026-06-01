package com.crm.mcsv_rrhh.service.bank.impl;

import com.crm.mcsv_rrhh.dto.bank.BankResponse;
import com.crm.mcsv_rrhh.entity.bank.Bank;
import com.crm.mcsv_rrhh.repository.bank.BankRepository;
import com.crm.mcsv_rrhh.service.bank.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {

    private final BankRepository repository;

    @Override
    public List<BankResponse> selectAll() {
        return repository.findAll().stream()
                .map(e -> BankResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
