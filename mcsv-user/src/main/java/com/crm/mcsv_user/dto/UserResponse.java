package com.crm.mcsv_user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean emailVerified;
    @JsonAlias("enabled")
    private Boolean status;
    private Set<RoleDTO> roles;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
