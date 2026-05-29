package com.crm.mcsv_auth.service.impl;

import com.crm.mcsv_auth.client.UserClient;
import com.crm.mcsv_auth.dto.UserDTO;
import com.crm.mcsv_auth.exception.AuthenticationException;
import com.crm.mcsv_auth.service.AuthUserLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserLookupServiceImpl implements AuthUserLookupService {

    private final UserClient userClient;

    @Override
    public UserDTO getByUsername(String username) {
        ResponseEntity<UserDTO> response = userClient.getUserByUsername(username);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    @Override
    public UserDTO getByEmail(String email) {
        ResponseEntity<UserDTO> response = userClient.getUserByEmail(email);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    @Override
    public UserDTO getById(Long id) {
        ResponseEntity<UserDTO> response = userClient.getUserById(id);
        if (response.getBody() == null) {
            throw new AuthenticationException("User not found");
        }
        return response.getBody();
    }

    @Override
    public UserDTO getByUsernameOrEmail(String usernameOrEmail) {
        try {
            return usernameOrEmail.contains("@")
                    ? getByEmail(usernameOrEmail)
                    : getByUsername(usernameOrEmail);
        } catch (Exception e) {
            log.debug("User not found by {}", usernameOrEmail.contains("@") ? "email" : "username");
        }

        throw new AuthenticationException("Invalid username or password");
    }
}
