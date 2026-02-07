package com.tyse.scrutiny.micro.notification.channel;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.service.TemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Email notification channel using JavaMailSender.
 */
@Component
public class EmailNotificationChannel implements NotificationChannelInterface {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationChannel.class);

    private final JavaMailSender mailSender;
    private final TemplateService templateService;

    @Value("${notification.from:no-reply@tyse-scrutiny.com}")
    private String fromAddress;

    public EmailNotificationChannel(JavaMailSender mailSender, TemplateService templateService) {
        this.mailSender = mailSender;
        this.templateService = templateService;
    }

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale) {
        return Mono.fromCallable(() -> {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
                );

                String content = templateService.processTemplate(templateName, templateData, locale);

                helper.setTo(recipient);
                helper.setFrom(fromAddress);
                helper.setSubject(subject != null ? subject : "Notificación Tyse Scrutiny");
                helper.setText(content, true);

                mailSender.send(mimeMessage);
                LOG.info("Email sent successfully to {} with template {}", recipient, templateName);
                return null;
            } catch (MailException | MessagingException e) {
                LOG.error("Failed to send email to {}: {}", recipient, e.getMessage(), e);
                throw new RuntimeException("Failed to send email", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }
}
