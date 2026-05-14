package com.crm.mcsv_user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredentialsRequest {

    private String usernameOrEmail;
    private String password;
}
