package com.tyse.scrutiny.micro.notification.web;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(NotificationResource.class)
class NotificationResourceTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NotificationDispatcherService dispatcherService;

    @Test
    void sendTestNotification_shouldReturnSuccess() {
        // Given
        when(dispatcherService.sendNotification(any())).thenReturn(Mono.empty());

        // When/Then
        webTestClient.post()
            .uri("/api/notifications/test?recipient=test@example.com")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("sent")
            .jsonPath("$.recipient").isEqualTo("test@example.com")
            .jsonPath("$.channel").isEqualTo("EMAIL");
    }

    @Test
    void sendTestNotification_withWhatsAppChannel_shouldReturnSuccess() {
        // Given
        when(dispatcherService.sendNotification(any())).thenReturn(Mono.empty());

        // When/Then
        webTestClient.post()
            .uri("/api/notifications/test?recipient=+573001234567&channel=WHATSAPP")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.channel").isEqualTo("WHATSAPP");
    }

    @Test
    void health_shouldReturnUpStatus() {
        // When/Then
        webTestClient.get()
            .uri("/api/notifications/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.service").isEqualTo("tyse-scrutiny-micro-notification");
    }
}
