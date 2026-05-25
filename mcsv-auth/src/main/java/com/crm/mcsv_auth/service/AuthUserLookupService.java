package com.crm.mcsv_auth.service;

import com.crm.mcsv_auth.dto.UserDTO;

public interface AuthUserLookupService {

    UserDTO getByUsername(String username);

    UserDTO getByEmail(String email);

    UserDTO getById(Long id);

    UserDTO getByUsernameOrEmail(String usernameOrEmail);
}
