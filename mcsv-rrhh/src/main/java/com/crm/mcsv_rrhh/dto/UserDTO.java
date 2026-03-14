package com.crm.mcsv_rrhh.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    @JsonAlias("status")
    private Boolean enabled;
    private Boolean emailVerified;
}
