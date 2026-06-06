package com.crm.mcsv_user.service;

public interface RoleNotificationService {

    void notifyRoleUpdated(Long roleId, String roleName);

    void notifyRoleStatusChanged(Long roleId, String roleName, boolean enabled);

    void notifyPermissionsChanged(Long roleId, String roleName, String userMessage);
}
