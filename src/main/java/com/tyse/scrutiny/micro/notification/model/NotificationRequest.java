package com.tyse.scrutiny.micro.notification.model;

import java.time.Instant;
import java.util.Map;

/**
 * Generic notification request received from notification-request topic.
 * Used for user account notifications (activation, password reset, etc.).
 */
public record NotificationRequest(
    NotificationType type,
    NotificationChannel channel,
    String recipient,
    Map<String, Object> templateData,
    String locale,
    Instant requestedAt
) {}
