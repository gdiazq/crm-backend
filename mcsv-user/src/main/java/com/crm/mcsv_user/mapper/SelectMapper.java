package com.crm.mcsv_user.mapper;

import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.dto.select.PermissionSelectItem;
import com.crm.mcsv_user.dto.select.RoleSelectItem;
import com.crm.mcsv_user.dto.select.UserEmailSelectItem;
import com.crm.mcsv_user.dto.select.UserSelectItem;
import com.crm.mcsv_user.entity.Permission;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.util.NameUtil;
import org.springframework.stereotype.Component;

/** Convierte entidades/DTOs a los items livianos {id, name/email} de los selectores. */
@Component
public class SelectMapper {

    public RoleSelectItem toRoleSelectItem(Role role) {
        return new RoleSelectItem(role.getId(), role.getName());
    }

    public PermissionSelectItem toPermissionSelectItem(Permission permission) {
        return new PermissionSelectItem(permission.getId(), permission.getName());
    }

    public UserSelectItem toUserSelectItem(User user) {
        return new UserSelectItem(user.getId(), NameUtil.fullName(user.getFirstName(), user.getLastName()));
    }

    public UserSelectItem toUserSelectItem(UserResponse user) {
        return new UserSelectItem(user.getId(), NameUtil.fullName(user.getFirstName(), user.getLastName()));
    }

    public UserEmailSelectItem toUserEmailSelectItem(UserResponse user) {
        return new UserEmailSelectItem(user.getId(), user.getEmail());
    }
}
