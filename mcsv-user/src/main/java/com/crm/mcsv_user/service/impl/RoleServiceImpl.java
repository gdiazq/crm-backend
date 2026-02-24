package com.crm.mcsv_user.service.impl;

import com.crm.mcsv_user.dto.RoleDTO;
import com.crm.mcsv_user.entity.Role;
import com.crm.mcsv_user.entity.User;
import com.crm.mcsv_user.exception.DuplicateResourceException;
import com.crm.mcsv_user.exception.ResourceNotFoundException;
import com.crm.mcsv_user.mapper.UserMapper;
import com.crm.mcsv_user.repository.RoleRepository;
import com.crm.mcsv_user.repository.UserRepository;
import com.crm.mcsv_user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll()
                .stream()
                .map(userMapper::roleToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> getAllRolesPaged(String search, Pageable pageable) {
        log.info("Fetching roles paged, search: {}", search);
        boolean hasSearch = search != null && !search.isBlank();
        if (hasSearch) {
            return roleRepository.searchRoles(search.trim(), pageable).map(userMapper::roleToDTO);
        }
        return roleRepository.findAll(pageable).map(userMapper::roleToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        log.info("Fetching role by id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return userMapper.roleToDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleByName(String name) {
        log.info("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return userMapper.roleToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO createRole(String name, String description) {
        log.info("Creating new role with name: {}", name);

        if (roleRepository.existsByName(name)) {
            throw new DuplicateResourceException("Role already exists with name: " + name);
        }

        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());

        return userMapper.roleToDTO(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);

        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }

        roleRepository.deleteById(id);
        log.info("Role deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Boolean enabled) {
        log.info("Updating status for role id: {} to {}", id, enabled);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        role.setEnabled(enabled);
        roleRepository.save(role);

        List<User> users = userRepository.findAllByRolesId(id);
        users.forEach(u -> u.setEnabled(enabled));
        userRepository.saveAll(users);

        log.info("Role status updated to {}. Affected {} users.", enabled, users.size());
    }
}
