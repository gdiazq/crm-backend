package com.crm.common.service.activeinactiveselect.impl;

import com.crm.common.dto.SelectItem;
import com.crm.common.enums.ActiveInactiveOption;
import com.crm.common.mapper.SelectItemMapper;
import com.crm.common.service.activeinactiveselect.ActiveInactiveSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiveInactiveSelectServiceImpl implements ActiveInactiveSelectService {

    private final SelectItemMapper selectItemMapper;

    @Override
    public List<SelectItem> getOptions() {
        return Arrays.stream(ActiveInactiveOption.values())
                .map(selectItemMapper::toSelectItem)
                .toList();
    }
}
