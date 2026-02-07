package com.tyse.scrutiny.micro.notification.web;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.model.NotificationRequest;
import com.tyse.scrutiny.micro.notification.model.NotificationType;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for manual notification sending and testing.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResource.class);

    private final NotificationDispatcherService dispatcherService;

    public NotificationResource(NotificationDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    /**
     * Send a test notification.
     */
    @PostMapping("/test")
    public Mono<ResponseEntity<Map<String, String>>> sendTestNotification(
        @RequestParam String recipient,
        @RequestParam(defaultValue = "EMAIL") NotificationChannel channel
    ) {
        LOG.info("REST request to send test notification to {} via {}", recipient, channel);

        NotificationRequest request = new NotificationRequest(
            NotificationType.SYSTEM_ALERT,
            channel,
            recipient,
            Map.of(
                "title", "Notificación de prueba",
                "message", "Este es un mensaje de prueba del servicio de notificaciones.",
                "timestamp", Instant.now().toString()
            ),
            "es",
            Instant.now()
        );

        return dispatcherService.sendNotification(request)
            .thenReturn(ResponseEntity.ok(Map.of(
                "status", "sent",
                "recipient", recipient,
                "channel", channel.name()
            )));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "tyse-scrutiny-micro-notification"
        )));
    }
}
