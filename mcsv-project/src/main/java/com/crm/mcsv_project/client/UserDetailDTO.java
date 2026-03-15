package com.crm.mcsv_project.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailDTO {
    private Long id;
    private String firstName;
    private String lastName;
}
