package com.crm.mcsv_user.service.impl;

import com.crm.common.exception.DuplicateResourceException;
import com.crm.common.exception.ResourceNotFoundException;
import com.crm.mcsv_user.dto.CreateUserRequest;
import com.crm.mcsv_user.dto.UserResponse;
import com.crm.mcsv_user.entity.EmailVerificationCode;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.EmailVerificationCodeRepository;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.UserNotificationService;
import com.crm.mcsv_user.service.UserProvisioningService;
import com.crm.mcsv_user.service.VerificationEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProvisioningServiceImpl implements UserProvisioningService {

    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserNotificationService userNotificationService;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final VerificationEmailService verificationEmailService;

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
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "Tmp!" + UUID.randomUUID();
        user.setPassword(passwordEncoder.encode(rawPassword));

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.getRoleId()));
            user.setRoles(new HashSet<>(Set.of(role)));
        } else {
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
            user.addRole(defaultRole);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        userNotificationService.sendWelcomeNotification(savedUser.getId(), savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void sendVerificationCode(Long userId, String email, String username) {
        log.info("Sending verification code for admin-created user id: {}", userId);

        // Invalidate any previous unused codes
        emailVerificationCodeRepository.deleteByUserIdAndUsedFalse(userId);

        // Generate 6-digit code
        String code = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);

        EmailVerificationCode verificationCode = EmailVerificationCode.builder()
                .code(code)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES))
                .used(false)
                .build();
        emailVerificationCodeRepository.save(verificationCode);

        verificationEmailService.sendVerificationEmail(email, username, code);
    }
}
