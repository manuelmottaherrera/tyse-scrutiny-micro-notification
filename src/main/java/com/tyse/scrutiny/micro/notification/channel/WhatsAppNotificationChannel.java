package com.tyse.scrutiny.micro.notification.channel;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.tyse.scrutiny.micro.notification.config.NotificationProperties;
import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.service.TemplateService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * WhatsApp notification channel using Twilio API.
 */
@Component
public class WhatsAppNotificationChannel implements NotificationChannelInterface {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppNotificationChannel.class);
    private static final String WHATSAPP_PREFIX = "whatsapp:";

    private final NotificationProperties properties;
    private final TemplateService templateService;
    private boolean twilioInitialized = false;

    public WhatsAppNotificationChannel(NotificationProperties properties, TemplateService templateService) {
        this.properties = properties;
        this.templateService = templateService;
    }

    @PostConstruct
    public void init() {
        if (isEnabled() && hasCredentials()) {
            initTwilio();
        }
    }

    private synchronized void initTwilio() {
        if (!twilioInitialized && hasCredentials()) {
            Twilio.init(
                properties.getTwilio().getAccountSid(),
                properties.getTwilio().getAuthToken()
            );
            twilioInitialized = true;
            LOG.info("Twilio initialized for WhatsApp notifications");
        }
    }

    private boolean hasCredentials() {
        return properties.getTwilio().getAccountSid() != null
            && !properties.getTwilio().getAccountSid().isBlank()
            && properties.getTwilio().getAuthToken() != null
            && !properties.getTwilio().getAuthToken().isBlank();
    }

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public boolean isEnabled() {
        return properties.getTwilio().getWhatsApp().isEnabled();
    }

    @Override
    public Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale) {
        if (!isEnabled()) {
            LOG.debug("WhatsApp channel is disabled, skipping notification to {}", recipient);
            return Mono.empty();
        }

        if (!hasCredentials()) {
            LOG.error("Twilio credentials not configured. Cannot send WhatsApp message.");
            return Mono.error(new IllegalStateException("Twilio credentials not configured"));
        }

        if (!twilioInitialized) {
            initTwilio();
        }

        return Mono.fromCallable(() -> {
            try {
                String plainTextContent = templateService.processTextTemplate(templateName, templateData, locale);

                String formattedRecipient = formatWhatsAppNumber(recipient);
                String fromNumber = properties.getTwilio().getWhatsApp().getFromNumber();

                if (!fromNumber.startsWith(WHATSAPP_PREFIX)) {
                    fromNumber = WHATSAPP_PREFIX + fromNumber;
                }

                Message message = Message.creator(
                    new PhoneNumber(formattedRecipient),
                    new PhoneNumber(fromNumber),
                    plainTextContent
                ).create();

                LOG.info("WhatsApp message sent successfully to {}. SID: {}", recipient, message.getSid());
                return null;
            } catch (Exception e) {
                LOG.error("Failed to send WhatsApp message to {}: {}", recipient, e.getMessage(), e);
                throw new RuntimeException("Failed to send WhatsApp message", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    private String formatWhatsAppNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        if (!cleaned.startsWith(WHATSAPP_PREFIX)) {
            cleaned = WHATSAPP_PREFIX + cleaned;
        }
        return cleaned;
    }
}
