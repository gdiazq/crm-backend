package com.crm.mcsv_auth.service.impl;

import com.crm.common.client.SqsEmailClient;
import com.crm.common.dto.EmailRequest;
import com.crm.mcsv_auth.service.VerificationEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private final SqsEmailClient sqsEmailClient;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String email, String username, String code) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String link = frontendUrl + "/verify-email?email=" + encodedEmail + "&code=" + code;

            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject("Verify your email address")
                    .templateName("verification-code")
                    .variables(Map.of(
                            "code", code,
                            "username", username,
                            "link", link
                    ))
                    .build();

            sqsEmailClient.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
        }
    }
}
