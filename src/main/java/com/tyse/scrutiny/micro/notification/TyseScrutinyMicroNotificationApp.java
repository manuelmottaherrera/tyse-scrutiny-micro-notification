package com.tyse.scrutiny.micro.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification microservice for Tyse Scrutiny.
 * Handles email, WhatsApp, and SMS notifications for:
 * - Anomaly alerts from e14-anomaly-detected topic
 * - User notifications from notification-request topic (account activation, password reset, etc.)
 */
@SpringBootApplication
public class TyseScrutinyMicroNotificationApp {

    public static void main(String[] args) {
        SpringApplication.run(TyseScrutinyMicroNotificationApp.class, args);
    }
}
