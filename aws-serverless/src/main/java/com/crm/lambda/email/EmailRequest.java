package com.crm.lambda.email;

import java.util.Map;

public class EmailRequest {

    private String to;

    private String subject;

    private String templateName;

    private Map<String, Object> variables;

    public EmailRequest() {}

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
