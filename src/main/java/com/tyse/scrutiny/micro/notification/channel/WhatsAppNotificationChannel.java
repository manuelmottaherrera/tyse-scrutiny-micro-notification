package com.tyse.scrutiny.micro.notification.channel;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * WhatsApp notification channel.
 * Placeholder implementation - to be integrated with WhatsApp Business API.
 */
@Component
public class WhatsAppNotificationChannel implements NotificationChannelInterface {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppNotificationChannel.class);

    @Value("${notification.whatsapp.enabled:false}")
    private boolean enabled;

    @Value("${notification.whatsapp.api-url:}")
    private String apiUrl;

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale) {
        if (!enabled) {
            LOG.debug("WhatsApp channel is disabled, skipping notification to {}", recipient);
            return Mono.empty();
        }

        // TODO: Implement WhatsApp Business API integration
        // This would typically use WebClient to call the WhatsApp Business API
        // with the template and recipient phone number

        LOG.warn("WhatsApp notification requested but not yet implemented. Recipient: {}, Template: {}", recipient, templateName);
        return Mono.empty();
    }
}
