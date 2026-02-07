package com.tyse.scrutiny.micro.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.micro.notification.model.AnomalyEvent;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Kafka consumer for e14-anomaly-detected topic.
 * Receives anomaly events and dispatches notifications.
 */
@Component("anomalyNotificationConsumer")
public class AnomalyNotificationConsumer implements Consumer<String> {

    private static final Logger LOG = LoggerFactory.getLogger(AnomalyNotificationConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationDispatcherService dispatcherService;

    public AnomalyNotificationConsumer(ObjectMapper objectMapper, NotificationDispatcherService dispatcherService) {
        this.objectMapper = objectMapper;
        this.dispatcherService = dispatcherService;
    }

    @Override
    public void accept(String message) {
        LOG.info("Received anomaly event from Kafka");
        LOG.debug("Anomaly event payload: {}", message);

        try {
            AnomalyEvent event = objectMapper.readValue(message, AnomalyEvent.class);
            LOG.info("Processing anomaly: id={}, type={}, severity={}, divipolKey={}",
                event.anomalyId(), event.type(), event.severity(), event.divipolKey());

            dispatcherService.sendAnomalyAlert(event)
                .doOnSuccess(v -> LOG.info("Anomaly notification sent successfully for: {}", event.anomalyId()))
                .doOnError(e -> LOG.error("Failed to send anomaly notification for {}: {}", event.anomalyId(), e.getMessage()))
                .subscribe();

        } catch (Exception e) {
            LOG.error("Failed to process anomaly event: {}", e.getMessage(), e);
        }
    }
}
