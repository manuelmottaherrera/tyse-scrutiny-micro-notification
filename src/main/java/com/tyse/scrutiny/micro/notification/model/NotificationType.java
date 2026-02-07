package com.tyse.scrutiny.micro.notification.model;

/**
 * Types of notifications that can be sent.
 */
public enum NotificationType {
    // Anomaly alerts
    ANOMALY_ALERT,

    // User account notifications (migrated from gateway)
    ACCOUNT_ACTIVATION,
    PASSWORD_RESET,
    USER_CREATION,

    // Generic notifications
    SYSTEM_ALERT,
    PROCESSING_COMPLETE,
    PROCESSING_FAILED
}
