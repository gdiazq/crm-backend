package com.crm.mcsv_user.service;

import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.UserResponse;

public interface UserProvisioningService {

    UserResponse createUser(CreateUserRequest request);

    void sendVerificationCode(Long userId, String email, String username);
}
