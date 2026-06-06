package com.crm.mcsv_user.service;

public interface VerificationEmailService {

    void sendVerificationEmail(String email, String username, String code);
}
