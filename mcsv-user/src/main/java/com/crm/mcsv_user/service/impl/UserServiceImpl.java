package com.crm.mcsv_user.service.impl;

import com.crm.mcsv_user.client.StorageClient;
import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.FileMetadataResponse;
import com.crm.mcsv_user.dto.UpdateUserRequest;
import com.crm.mcsv_user.dto.UserDTO;
import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.entity.UserProfile;
import com.crm.mcsv_user.exception.DuplicateResourceException;
import com.crm.mcsv_user.exception.ResourceNotFoundException;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserProfileRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final StorageClient storageClient;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        log.info("Fetching users page: {}, size: {}, search: {}", pageable.getPageNumber(), pageable.getPageSize(), search);
        if (search != null && !search.isBlank()) {
            return userRepository.searchUsers(search.trim(), pageable)
                    .map(userMapper::toResponse);
        }
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.info("Fetching user by username: {}", username);
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
            user.addRole(defaultRole);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getStatus() != null) {
            user.setEnabled(request.getStatus());
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long userId) {
        log.info("Updating last login for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAvatarUrl(Long userId, String avatarUrl) {
        log.info("Updating avatar URL for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .avatarUrl(avatarUrl)
                    .build();
        } else {
            profile.setAvatarUrl(avatarUrl);
        }
        userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public boolean validateCredentials(String usernameOrEmail, String password) {
        log.info("Validating credentials for: {}", usernameOrEmail);

        // Buscar al usuario por username o email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElse(userRepository.findByEmail(usernameOrEmail).orElse(null));

        if (user == null) {
            log.warn("User not found for validation: {}", usernameOrEmail);
            return false;
        }

        // Verificar si la cuenta está activa
        if (!user.getEnabled() || !user.getAccountNonLocked()) {
            log.warn("Inactive or locked account for user: {}", usernameOrEmail);
            return false;
        }

        // Validar la contraseña
        boolean isValid = passwordEncoder.matches(password, user.getPassword());

        if (isValid) {
            log.info("Credentials validated successfully for user: {}", user.getUsername());
            // Actualizar la última fecha de login
            updateLastLogin(user.getId());
        } else {
            log.warn("Invalid password for user: {}", user.getUsername());
        }

        return isValid;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        log.info("Updating password for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId) {
        log.info("Verifying email for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for user id: {}", userId);
    }

    @Override
    @Transactional
    public Map<String, String> uploadAvatar(Long userId, MultipartFile file) {
        log.info("Uploading avatar for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Delete previous avatar from storage if exists
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile != null && profile.getAvatarUrl() != null) {
            try {
                ResponseEntity<List<FileMetadataResponse>> existing =
                        storageClient.listByEntity("USER_AVATAR", userId);
                if (existing.getBody() != null) {
                    for (FileMetadataResponse meta : existing.getBody()) {
                        storageClient.delete(meta.getId(), userId);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to delete previous avatar for user {}: {}", userId, e.getMessage());
            }
        }

        // Upload new avatar to storage (public)
        ResponseEntity<FileMetadataResponse> uploadResponse =
                storageClient.upload(file, userId, "USER_AVATAR", userId, true);

        if (uploadResponse.getBody() == null || uploadResponse.getBody().getUrl() == null) {
            throw new RuntimeException("Failed to upload avatar to storage");
        }

        String avatarUrl = uploadResponse.getBody().getUrl();

        // Save URL in UserProfile
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .avatarUrl(avatarUrl)
                    .build();
        } else {
            profile.setAvatarUrl(avatarUrl);
        }
        userProfileRepository.save(profile);

        log.info("Avatar uploaded successfully for user id: {}", userId);
        return Map.of("avatarUrl", avatarUrl);
    }
}
