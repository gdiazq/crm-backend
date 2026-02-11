package com.crm.mcsv_user.service;

import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.UpdateUserRequest;
import com.crm.mcsv_user.dto.UserDTO;
import com.crm.mcsv_user.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void updateLastLogin(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean validateCredentials(String usernameOrEmail, String password);

    void updatePassword(Long userId, String newPassword);

    void verifyEmail(Long userId);
}
