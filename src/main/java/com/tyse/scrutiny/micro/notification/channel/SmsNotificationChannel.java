package com.tyse.scrutiny.micro.notification.channel;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * SMS notification channel.
 * Placeholder implementation - to be integrated with SMS provider (Twilio, etc.).
 */
@Component
public class SmsNotificationChannel implements NotificationChannelInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SmsNotificationChannel.class);

    @Value("${notification.sms.enabled:false}")
    private boolean enabled;

    @Value("${notification.sms.provider:twilio}")
    private String provider;

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Mono<Void> send(String recipient, String templateName, Map<String, Object> templateData, String subject, String locale) {
        if (!enabled) {
            LOG.debug("SMS channel is disabled, skipping notification to {}", recipient);
            return Mono.empty();
        }

        // TODO: Implement SMS provider integration (Twilio, AWS SNS, etc.)
        // This would typically use WebClient to call the provider's API
        // with the message and recipient phone number

        LOG.warn("SMS notification requested but not yet implemented. Recipient: {}, Template: {}", recipient, templateName);
        return Mono.empty();
    }
}
