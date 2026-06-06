package com.crm.mcsv_user.service.impl;

import com.crm.common.dto.SendNotificationRequest;
import com.crm.common.exception.DuplicateResourceException;
import com.crm.mcsv_user.dto.CreateRoleRequest;
import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.event.NotificationBatchEvent;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleProvisioningServiceImpl implements RoleProvisioningService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RoleDTO createRole(CreateRoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role already exists with name: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());

        List<SendNotificationRequest> notifications = new ArrayList<>();
        roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRole ->
                userRepository.findAllByRolesId(adminRole.getId()).forEach(admin ->
                        notifications.add(SendNotificationRequest.builder()
                                .userId(admin.getId())
                                .title("Nuevo rol creado")
                                .message("Se ha creado el rol \"" + savedRole.getName() + "\" en el sistema.")
                                .type("INFO")
                                .build())));
        if (!notifications.isEmpty()) {
            eventPublisher.publishEvent(new NotificationBatchEvent(notifications));
        }

        return userMapper.roleToDTO(savedRole);
    }
}
