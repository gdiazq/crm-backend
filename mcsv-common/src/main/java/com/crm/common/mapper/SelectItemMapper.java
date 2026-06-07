package com.crm.common.mapper;

import com.crm.common.dto.SelectItem;
import com.crm.common.enums.SelectableOption;
import org.springframework.stereotype.Component;

@Component
public class SelectItemMapper {

    public SelectItem toSelectItem(SelectableOption option) {
        return new SelectItem(option.getId(), option.getValue(), option.getDisplayName());
    }
}
