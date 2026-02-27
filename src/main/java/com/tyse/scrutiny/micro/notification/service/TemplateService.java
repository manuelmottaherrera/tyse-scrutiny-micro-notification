package com.tyse.scrutiny.micro.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

/**
 * Service for processing Thymeleaf templates (email HTML and SMS/WhatsApp text).
 */
@Service
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);

    private final TemplateEngine templateEngine;

    public TemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Process a Thymeleaf HTML template with the given data.
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

    /**
     * Process a Thymeleaf text template for SMS/WhatsApp.
     * Falls back to HTML template with text extraction if no text template exists.
     *
     * @param templateName Name of the template (without extension)
     * @param templateData Data to inject into the template
     * @param locale       Locale for the template (default: es)
     * @return Rendered text content
     */
    public String processTextTemplate(String templateName, Map<String, Object> templateData, String locale) {
        try {
            Context context = new Context(locale != null ? Locale.forLanguageTag(locale) : new Locale("es"));
            if (templateData != null) {
                templateData.forEach(context::setVariable);
            }

            if (textTemplateExists(templateName)) {
                String rendered = templateEngine.process("sms/" + templateName, context);
                LOG.debug("Text template {} processed successfully", templateName);
                return rendered.trim();
            }

            LOG.debug("Text template {} not found, falling back to HTML template", templateName);
            String htmlContent = templateEngine.process("mail/" + templateName, context);
            return extractPlainText(htmlContent);
        } catch (Exception e) {
            LOG.error("Failed to process text template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Failed to process text template: " + templateName, e);
        }
    }

    private boolean textTemplateExists(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/sms/" + templateName + ".txt");
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    private String extractPlainText(String htmlContent) {
        return htmlContent
            .replaceAll("<style[^>]*>[\\s\\S]*?</style>", "")
            .replaceAll("<script[^>]*>[\\s\\S]*?</script>", "")
            .replaceAll("<br\\s*/?>", "\n")
            .replaceAll("</p>", "\n\n")
            .replaceAll("</div>", "\n")
            .replaceAll("</tr>", "\n")
            .replaceAll("</li>", "\n")
            .replaceAll("<[^>]+>", "")
            .replaceAll("&nbsp;", " ")
            .replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&quot;", "\"")
            .replaceAll("&#39;", "'")
            .replaceAll("[ \\t]+", " ")
            .replaceAll("\\n{3,}", "\n\n")
            .trim();
    }
}
