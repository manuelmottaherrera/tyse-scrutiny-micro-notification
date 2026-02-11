package com.tyse.scrutiny.micro.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for notification settings.
 */
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private final AnomalyAlert anomalyAlert = new AnomalyAlert();
    private String baseUrl = "http://localhost:8080";
    private String from = "no-reply@tyse-scrutiny.com";

    public AnomalyAlert getAnomalyAlert() {
        return anomalyAlert;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public static class AnomalyAlert {
        private List<String> defaultRecipients = new ArrayList<>();
        private String minimumSeverity = "MEDIUM";

        public List<String> getDefaultRecipients() {
            return defaultRecipients;
        }

        public void setDefaultRecipients(List<String> defaultRecipients) {
            this.defaultRecipients = defaultRecipients;
        }

        public String getMinimumSeverity() {
            return minimumSeverity;
        }

        public void setMinimumSeverity(String minimumSeverity) {
            this.minimumSeverity = minimumSeverity;
        }
    }
}
