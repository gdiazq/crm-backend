package com.crm.common.service.activeinactiveselect;

import com.crm.common.dto.SelectItem;

import java.util.List;

public interface ActiveInactiveSelectService {

    List<SelectItem> getOptions();
}
