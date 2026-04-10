package com.crm.lambda.email;

import java.util.Map;

public class EmailRequest {

    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;

    public EmailRequest() {}

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
