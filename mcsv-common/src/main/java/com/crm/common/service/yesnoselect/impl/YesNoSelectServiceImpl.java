package com.crm.common.service.yesnoselect.impl;

import com.crm.common.dto.SelectItem;
import com.crm.common.enums.YesNoOption;
import com.crm.common.mapper.SelectItemMapper;
import com.crm.common.service.yesnoselect.YesNoSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YesNoSelectServiceImpl implements YesNoSelectService {

    private final SelectItemMapper selectItemMapper;

    @Override
    public List<SelectItem> getOptions() {
        return Arrays.stream(YesNoOption.values())
                .map(selectItemMapper::toSelectItem)
                .toList();
    }
}
