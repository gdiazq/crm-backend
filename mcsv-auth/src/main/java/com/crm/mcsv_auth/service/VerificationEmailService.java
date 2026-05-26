package com.crm.mcsv_auth.service;

public interface VerificationEmailService {

    void sendVerificationEmail(String email, String username, String code);
}
