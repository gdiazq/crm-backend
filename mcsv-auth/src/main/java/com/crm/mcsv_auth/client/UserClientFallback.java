package com.crm.mcsv_auth.client;

import com.crm.mcsv_auth.dto.CreateUserInternalRequest;
import com.crm.mcsv_auth.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public ResponseEntity<UserDTO> getUserByUsername(String username) {
        throw new UserClientUnavailableException("User service unavailable: cannot get user by username");
    }

    @Override
    public ResponseEntity<UserDTO> getUserByEmail(String email) {
        throw new UserClientUnavailableException("User service unavailable: cannot get user by email");
    }

    @Override
    public ResponseEntity<UserDTO> getUserById(Long id) {
        throw new UserClientUnavailableException("User service unavailable: cannot get user by id");
    }

    @Override
    public ResponseEntity<UserDTO> signUpUser(CreateUserInternalRequest request) {
        throw new UserClientUnavailableException("User service unavailable: cannot sign up user");
    }

    @Override
    public ResponseEntity<Boolean> validateCredentials(CredentialsRequest credentials) {
        throw new UserClientUnavailableException("User service unavailable: cannot validate credentials");
    }

    @Override
    public ResponseEntity<Void> updatePassword(UpdatePasswordRequest request) {
        throw new UserClientUnavailableException("User service unavailable: cannot update password");
    }

    @Override
    public ResponseEntity<Void> verifyEmail(Long id) {
        throw new UserClientUnavailableException("User service unavailable: cannot verify email");
    }

    @Override
    public ResponseEntity<Void> updateLastLogin(Long id) {
        throw new UserClientUnavailableException("User service unavailable: cannot update last login");
    }

    @Override
    public ResponseEntity<Void> updateAvatarUrl(Long id, Map<String, String> body) {
        throw new UserClientUnavailableException("User service unavailable: cannot update avatar URL");
    }

    @Override
    public ResponseEntity<Boolean> validateAndConsumeCode(Long userId, Map<String, String> body) {
        throw new UserClientUnavailableException("User service unavailable: cannot validate email code");
    }

    public static class UserClientUnavailableException extends RuntimeException {
        public UserClientUnavailableException(String message) {
            super(message);
        }
    }
}
