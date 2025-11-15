package com.crm.mcsv_user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private Long id;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String bio;
    private String company;
    private String position;
    private String websiteUrl;
    private String linkedinUrl;
    private String twitterUrl;
}
