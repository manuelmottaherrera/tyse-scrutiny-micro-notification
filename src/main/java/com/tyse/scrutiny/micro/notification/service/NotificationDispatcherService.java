package com.tyse.scrutiny.micro.notification.service;

import com.tyse.scrutiny.micro.notification.channel.NotificationChannelInterface;
import com.tyse.scrutiny.micro.notification.config.NotificationProperties;
import com.tyse.scrutiny.micro.notification.model.AnomalyEvent;
import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.model.NotificationRequest;
import com.tyse.scrutiny.micro.notification.model.NotificationType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that dispatches notifications to appropriate channels.
 */
@Service
public class NotificationDispatcherService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDispatcherService.class);

    private final List<NotificationChannelInterface> channels;
    private final NotificationProperties properties;

    public NotificationDispatcherService(List<NotificationChannelInterface> channels, NotificationProperties properties) {
        this.channels = channels;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        LOG.info("NotificationDispatcherService initialized with {} anomaly recipients: {}",
            properties.getAnomalyAlert().getDefaultRecipients().size(),
            properties.getAnomalyAlert().getDefaultRecipients());
    }

    /**
     * Send an anomaly alert notification.
     */
    public Mono<Void> sendAnomalyAlert(AnomalyEvent event) {
        List<String> recipients = properties.getAnomalyAlert().getDefaultRecipients();
        LOG.info("sendAnomalyAlert called for anomaly: {}, recipients configured: {}",
            event.anomalyId(), recipients);

        if (!shouldSendAnomaly(event)) {
            LOG.debug("Skipping anomaly notification for {} - below minimum severity", event.anomalyId());
            return Mono.empty();
        }

        if (recipients == null || recipients.isEmpty()) {
            LOG.warn("No default anomaly recipients configured! Check notification.anomaly-alert.default-recipients");
            return Mono.empty();
        }

        Map<String, Object> templateData = buildAnomalyTemplateData(event);
        String subject = buildAnomalySubject(event);

        return Flux.fromIterable(recipients)
            .flatMap(recipient -> sendToChannel(
                NotificationChannel.EMAIL,
                recipient,
                "anomalyAlert",
                templateData,
                subject,
                "es"
            ))
            .then();
    }

    /**
     * Send a generic notification request.
     */
    public Mono<Void> sendNotification(NotificationRequest request) {
        String templateName = getTemplateForType(request.type());
        String subject = getSubjectForType(request.type(), request.templateData());
        NotificationChannel channel = request.channel() != null ? request.channel() : NotificationChannel.EMAIL;

        if (channel == NotificationChannel.ALL) {
            return Flux.fromIterable(channels)
                .filter(NotificationChannelInterface::isEnabled)
                .flatMap(ch -> ch.send(request.recipient(), templateName, request.templateData(), subject, request.locale()))
                .then();
        }

        return sendToChannel(channel, request.recipient(), templateName, request.templateData(), subject, request.locale());
    }

    private Mono<Void> sendToChannel(NotificationChannel channel, String recipient, String templateName,
                                      Map<String, Object> templateData, String subject, String locale) {
        return Flux.fromIterable(channels)
            .filter(ch -> ch.getChannelType() == channel && ch.isEnabled())
            .next()
            .flatMap(ch -> ch.send(recipient, templateName, templateData, subject, locale))
            .doOnError(e -> LOG.error("Failed to send notification via {}: {}", channel, e.getMessage()))
            .onErrorResume(e -> Mono.empty());
    }

    private boolean shouldSendAnomaly(AnomalyEvent event) {
        List<String> severityOrder = List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
        int eventIndex = severityOrder.indexOf(event.severity());
        int minIndex = severityOrder.indexOf(properties.getAnomalyAlert().getMinimumSeverity());
        return eventIndex >= minIndex;
    }

    private Map<String, Object> buildAnomalyTemplateData(AnomalyEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("anomalyId", event.anomalyId());
        data.put("type", event.type());
        data.put("severity", event.severity());
        data.put("divipolKey", event.divipolKey());
        data.put("depCode", event.depCode());
        data.put("munCode", event.munCode());
        data.put("table", event.table());
        data.put("partyNumber", event.partyNumber());
        data.put("partyName", event.partyName());
        data.put("candidateId", event.candidateId());
        data.put("candidateFirstName", event.candidateFirstName());
        data.put("candidateLastName", event.candidateLastName());
        data.put("precountVotes", event.precountVotes());
        data.put("scrutinyVotes", event.scrutinyVotes());
        data.put("difference", event.difference());
        data.put("scrutinyDate", event.scrutinyDate());
        data.put("electionProcessName", event.electionProcessName());
        data.put("detectedAt", event.detectedAt());
        data.put("baseUrl", properties.getBaseUrl());
        data.put("anomalyUrl", properties.getBaseUrl() + "/anomalies/" + event.anomalyId());
        return data;
    }

    private String buildAnomalySubject(AnomalyEvent event) {
        return String.format("[%s] Anomalía detectada: %s - Mesa %s",
            event.severity(),
            event.type(),
            event.table()
        );
    }

    private String getTemplateForType(NotificationType type) {
        return switch (type) {
            case ANOMALY_ALERT -> "anomalyAlert";
            case ACCOUNT_ACTIVATION -> "activationEmail";
            case PASSWORD_RESET -> "passwordResetEmail";
            case USER_CREATION -> "creationEmail";
            default -> "genericNotification";
        };
    }

    private String getSubjectForType(NotificationType type, Map<String, Object> templateData) {
        return switch (type) {
            case ACCOUNT_ACTIVATION -> "Activación de cuenta - Tyse Scrutiny";
            case PASSWORD_RESET -> "Restablecer contraseña - Tyse Scrutiny";
            case USER_CREATION -> "Cuenta creada - Tyse Scrutiny";
            case PROCESSING_COMPLETE -> "Procesamiento completado - Tyse Scrutiny";
            case PROCESSING_FAILED -> "Error en procesamiento - Tyse Scrutiny";
            default -> "Notificación - Tyse Scrutiny";
        };
    }
}
