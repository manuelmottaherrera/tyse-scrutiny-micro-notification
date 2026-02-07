package com.tyse.scrutiny.micro.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.model.NotificationRequest;
import com.tyse.scrutiny.micro.notification.model.NotificationType;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRequestConsumerTest {

    @Mock
    private NotificationDispatcherService dispatcherService;

    private ObjectMapper objectMapper;
    private NotificationRequestConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new NotificationRequestConsumer(objectMapper, dispatcherService);
    }

    @Test
    void accept_accountActivation_shouldDispatchNotification() throws Exception {
        // Given
        String json = createNotificationRequestJson(NotificationType.ACCOUNT_ACTIVATION);
        when(dispatcherService.sendNotification(any(NotificationRequest.class)))
            .thenReturn(Mono.empty());

        // When
        consumer.accept(json);

        // Then
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(dispatcherService).sendNotification(captor.capture());

        NotificationRequest request = captor.getValue();
        assertThat(request.type()).isEqualTo(NotificationType.ACCOUNT_ACTIVATION);
        assertThat(request.channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(request.recipient()).isEqualTo("user@test.com");
    }

    @Test
    void accept_passwordReset_shouldDispatchNotification() throws Exception {
        // Given
        String json = createNotificationRequestJson(NotificationType.PASSWORD_RESET);
        when(dispatcherService.sendNotification(any(NotificationRequest.class)))
            .thenReturn(Mono.empty());

        // When
        consumer.accept(json);

        // Then
        verify(dispatcherService).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void accept_invalidJson_shouldNotThrowException() {
        // Given
        String invalidJson = "not valid json";

        // When/Then - should not throw
        consumer.accept(invalidJson);

        verify(dispatcherService, never()).sendNotification(any());
    }

    @Test
    void accept_nullMessage_shouldNotThrowException() {
        // When/Then - should not throw
        consumer.accept(null);

        verify(dispatcherService, never()).sendNotification(any());
    }

    private String createNotificationRequestJson(NotificationType type) throws Exception {
        NotificationRequest request = new NotificationRequest(
            type,
            NotificationChannel.EMAIL,
            "user@test.com",
            Map.of("user", Map.of("firstName", "Test")),
            "es",
            Instant.now()
        );
        return objectMapper.writeValueAsString(request);
    }
}
