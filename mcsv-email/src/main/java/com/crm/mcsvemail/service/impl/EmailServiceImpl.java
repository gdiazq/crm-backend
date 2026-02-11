package com.crm.mcsvemail.service.impl;

import com.crm.mcsvemail.dto.EmailRequest;
import com.crm.mcsvemail.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendEmail(EmailRequest request) {
        log.info("Sending email to: {} with template: {}", request.getTo(), request.getTemplateName());

        try {
            Context context = new Context();
            if (request.getVariables() != null) {
                request.getVariables().forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(request.getTemplateName(), context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", request.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", request.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
