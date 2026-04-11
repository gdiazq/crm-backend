package com.crm.lambda.email;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

public class TemplateRenderer {

    private final TemplateEngine engine;

    public TemplateRenderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    public String render(String templateName, Map<String, Object> variables) {
        Context ctx = new Context();
        if (variables != null) {
            variables.forEach(ctx::setVariable);
        }
        return engine.process(templateName, ctx);
    }
}
