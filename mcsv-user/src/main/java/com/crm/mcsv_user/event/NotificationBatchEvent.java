package com.crm.mcsv_user.event;

import com.crm.common.dto.SendNotificationRequest;

import java.util.List;

public record NotificationBatchEvent(List<SendNotificationRequest> notifications) {
}
