package com.crm.lambda.email;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

public class EmailHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger log = LoggerFactory.getLogger(EmailHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TemplateRenderer renderer = new TemplateRenderer();
    private static final SesClient ses = SesClient.create();
    private static final String FROM_EMAIL = System.getenv("FROM_EMAIL");

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage msg : event.getRecords()) {
            try {
                EmailRequest req = mapper.readValue(msg.getBody(), EmailRequest.class);
                log.info("Processing email to: {} template: {}", req.getTo(), req.getTemplateName());

                String html = renderer.render(req.getTemplateName(), req.getVariables());

                ses.sendEmail(SendEmailRequest.builder()
                        .source(FROM_EMAIL)
                        .destination(Destination.builder()
                                .toAddresses(req.getTo())
                                .build())
                        .message(Message.builder()
                                .subject(Content.builder()
                                        .data(req.getSubject())
                                        .charset("UTF-8")
                                        .build())
                                .body(Body.builder()
                                        .html(Content.builder()
                                                .data(html)
                                                .charset("UTF-8")
                                                .build())
                                        .build())
                                .build())
                        .build());

                log.info("Email sent successfully to: {}", req.getTo());
            } catch (Exception e) {
                log.error("Failed to process SQS message: {}", msg.getMessageId(), e);
                throw new RuntimeException("Failed to send email", e);
            }
        }
        return null;
    }
}
