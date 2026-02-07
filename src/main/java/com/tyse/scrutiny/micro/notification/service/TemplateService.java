package com.tyse.scrutiny.micro.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * Service for processing Thymeleaf email templates.
 */
@Service
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateEngine templateEngine;

    public TemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Process a Thymeleaf template with the given data.
     *
     * @param templateName Name of the template (without .html extension)
     * @param templateData Data to inject into the template
     * @param locale       Locale for the template (default: es)
     * @return Rendered HTML content
     */
    public String processTemplate(String templateName, Map<String, Object> templateData, String locale) {
        try {
            Context context = new Context(locale != null ? Locale.forLanguageTag(locale) : new Locale("es"));
            if (templateData != null) {
                templateData.forEach(context::setVariable);
            }

            String rendered = templateEngine.process("mail/" + templateName, context);
            LOG.debug("Template {} processed successfully", templateName);
            return rendered;
        } catch (Exception e) {
            LOG.error("Failed to process template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Failed to process template: " + templateName, e);
        }
    }
}
