package com.tyse.scrutiny.micro.notification.channel;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Interface for notification channels (Email, WhatsApp, SMS).
 */
public interface NotificationChannelInterface {

    /**
     * Get the channel type.
     */
    NotificationChannel getChannelType();

    /**
     * Check if this channel is enabled.
     */
    boolean isEnabled();

    /**
     * Send a notification using a template.
     *
     * @param recipient    The recipient (email, phone number, etc.)
     * @param templateName The template to use
     * @param templateData Data for the template
     * @param subject      Subject line (for email) or null
     * @param locale       Locale for the template
     * @return Mono<Void> that completes when the notification is sent
     */
    Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale);
}
