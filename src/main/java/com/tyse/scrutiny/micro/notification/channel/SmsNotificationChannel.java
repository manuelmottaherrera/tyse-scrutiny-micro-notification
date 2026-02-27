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
 * SMS notification channel using Twilio API.
 */
@Component
public class SmsNotificationChannel implements NotificationChannelInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SmsNotificationChannel.class);
    private static final int MAX_SMS_LENGTH = 1600;

    private final NotificationProperties properties;
    private final TemplateService templateService;
    private boolean twilioInitialized = false;

    public SmsNotificationChannel(NotificationProperties properties, TemplateService templateService) {
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
            LOG.info("Twilio initialized for SMS notifications");
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
        return NotificationChannel.SMS;
    }

    @Override
    public boolean isEnabled() {
        return properties.getTwilio().getSms().isEnabled();
    }

    @Override
    public Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale) {
        if (!isEnabled()) {
            LOG.debug("SMS channel is disabled, skipping notification to {}", recipient);
            return Mono.empty();
        }

        if (!hasCredentials()) {
            LOG.error("Twilio credentials not configured. Cannot send SMS.");
            return Mono.error(new IllegalStateException("Twilio credentials not configured"));
        }

        String fromNumber = properties.getTwilio().getSms().getFromNumber();
        if (fromNumber == null || fromNumber.isBlank()) {
            LOG.error("SMS from number not configured. Cannot send SMS.");
            return Mono.error(new IllegalStateException("SMS from number not configured"));
        }

        if (!twilioInitialized) {
            initTwilio();
        }

        return Mono.fromCallable(() -> {
            try {
                String plainTextContent = templateService.processTextTemplate(templateName, templateData, locale);
                String smsContent = truncateForSms(plainTextContent);

                String formattedRecipient = formatPhoneNumber(recipient);

                Message message = Message.creator(
                    new PhoneNumber(formattedRecipient),
                    new PhoneNumber(fromNumber),
                    smsContent
                ).create();

                LOG.info("SMS sent successfully to {}. SID: {}", recipient, message.getSid());
                return null;
            } catch (Exception e) {
                LOG.error("Failed to send SMS to {}: {}", recipient, e.getMessage(), e);
                throw new RuntimeException("Failed to send SMS", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    private String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }
        return cleaned;
    }

    private String truncateForSms(String content) {
        if (content.length() <= MAX_SMS_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_SMS_LENGTH - 3) + "...";
    }
}
