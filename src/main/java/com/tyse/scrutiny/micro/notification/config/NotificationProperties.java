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
    private final Twilio twilio = new Twilio();
    private String baseUrl = "http://localhost:8080";
    private String from = "no-reply@tyse-scrutiny.com";

    public AnomalyAlert getAnomalyAlert() {
        return anomalyAlert;
    }

    public Twilio getTwilio() {
        return twilio;
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

    public static class Twilio {
        private String accountSid = "";
        private String authToken = "";
        private final WhatsApp whatsApp = new WhatsApp();
        private final Sms sms = new Sms();

        public String getAccountSid() {
            return accountSid;
        }

        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public WhatsApp getWhatsApp() {
            return whatsApp;
        }

        public Sms getSms() {
            return sms;
        }

        public static class WhatsApp {
            private boolean enabled = false;
            private String fromNumber = "";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getFromNumber() {
                return fromNumber;
            }

            public void setFromNumber(String fromNumber) {
                this.fromNumber = fromNumber;
            }
        }

        public static class Sms {
            private boolean enabled = false;
            private String fromNumber = "";

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getFromNumber() {
                return fromNumber;
            }

            public void setFromNumber(String fromNumber) {
                this.fromNumber = fromNumber;
            }
        }
    }
}
