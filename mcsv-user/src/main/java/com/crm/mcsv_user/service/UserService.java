package com.crm.mcsv_user.service;

import com.crm.common.dto.BulkImportResult;
import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.UpdateUserRequest;
import com.crm.mcsv_user.dto.UserDTO;
import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.dto.select.UserEmailSelectItem;
import com.crm.mcsv_user.dto.select.UserSelectItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {

    Page<UserResponse> filterUsers(String name, String email, Boolean status, Long roleId, Pageable pageable, String sortBy, String sortDir);

    List<UserResponse> getAllUsersForSelect();

    List<UserResponse> getAvailableUsersForEmployee(String search, List<Long> excludeIds);

    List<UserSelectItem> selectUserNames();

    List<UserEmailSelectItem> selectUserEmails();

    List<UserSelectItem> selectAvailableForEmployee(String search, List<Long> excludeIds);

    List<UserSelectItem> selectUsersByRole(String roleKeyword);

    UserDTO getUserById(Long id);

    List<UserDTO> getUsersByIds(List<Long> ids);

    UserDTO getUserByUsername(String username);

    UserDTO getUserByEmail(String email);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void updateStatus(Long id, Boolean enabled);

    void updateLastLogin(Long userId);

    void updateAvatarUrl(Long userId, String avatarUrl);

    boolean validateCredentials(String usernameOrEmail, String password);

    void updatePassword(Long userId, String newPassword);

    void verifyEmail(Long userId);

    Map<String, String> uploadAvatar(Long userId, MultipartFile file);

    void sendVerificationCode(Long userId, String email, String username);

    boolean validateAndConsumeCode(Long userId, String code);

    Map<String, Long> getUserStats();

    byte[] exportCsv();

    BulkImportResult importUsersFromCsv(MultipartFile file);
}
