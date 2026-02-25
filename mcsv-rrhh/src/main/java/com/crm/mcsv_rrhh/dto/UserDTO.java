package com.crm.mcsv_rrhh.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Boolean enabled;
    private Boolean emailVerified;
}
