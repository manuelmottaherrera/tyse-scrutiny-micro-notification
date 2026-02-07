package com.tyse.scrutiny.micro.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.micro.notification.model.NotificationRequest;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Kafka consumer for notification-request topic.
 * Receives generic notification requests from other services
 * (e.g., account activation, password reset from gateway).
 */
@Component("notificationRequestConsumer")
public class NotificationRequestConsumer implements Consumer<String> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRequestConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationDispatcherService dispatcherService;

    public NotificationRequestConsumer(ObjectMapper objectMapper, NotificationDispatcherService dispatcherService) {
        this.objectMapper = objectMapper;
        this.dispatcherService = dispatcherService;
    }

    @Override
    public void accept(String message) {
        LOG.info("Received notification request from Kafka");
        LOG.debug("Notification request payload: {}", message);

        try {
            NotificationRequest request = objectMapper.readValue(message, NotificationRequest.class);
            LOG.info("Processing notification request: type={}, channel={}, recipient={}",
                request.type(), request.channel(), request.recipient());

            dispatcherService.sendNotification(request)
                .doOnSuccess(v -> LOG.info("Notification sent successfully to: {}", request.recipient()))
                .doOnError(e -> LOG.error("Failed to send notification to {}: {}", request.recipient(), e.getMessage()))
                .subscribe();

        } catch (Exception e) {
            LOG.error("Failed to process notification request: {}", e.getMessage(), e);
        }
    }
}
