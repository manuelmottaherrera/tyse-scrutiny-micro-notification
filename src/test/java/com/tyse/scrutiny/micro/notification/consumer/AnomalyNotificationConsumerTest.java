package com.tyse.scrutiny.micro.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tyse.scrutiny.micro.notification.model.AnomalyEvent;
import com.tyse.scrutiny.micro.notification.service.NotificationDispatcherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnomalyNotificationConsumerTest {

    @Mock
    private NotificationDispatcherService dispatcherService;

    private ObjectMapper objectMapper;
    private AnomalyNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new AnomalyNotificationConsumer(objectMapper, dispatcherService);
    }

    @Test
    void accept_validMessage_shouldDispatchNotification() throws Exception {
        // Given
        String json = createAnomalyEventJson();
        when(dispatcherService.sendAnomalyAlert(any(AnomalyEvent.class)))
            .thenReturn(Mono.empty());

        // When
        consumer.accept(json);

        // Then
        ArgumentCaptor<AnomalyEvent> captor = ArgumentCaptor.forClass(AnomalyEvent.class);
        verify(dispatcherService).sendAnomalyAlert(captor.capture());

        AnomalyEvent event = captor.getValue();
        assertThat(event.type()).isEqualTo("PRECOUNT_DIFFERENCE");
        assertThat(event.severity()).isEqualTo("HIGH");
        assertThat(event.divipolKey()).isEqualTo("76-001-01-01-001");
    }

    @Test
    void accept_invalidJson_shouldNotThrowException() {
        // Given
        String invalidJson = "{ invalid json }";

        // When/Then - should not throw
        consumer.accept(invalidJson);

        // Verify dispatcher was not called
        verify(dispatcherService, never()).sendAnomalyAlert(any());
    }

    @Test
    void accept_emptyMessage_shouldNotThrowException() {
        // When/Then - should not throw
        consumer.accept("");

        verify(dispatcherService, never()).sendAnomalyAlert(any());
    }

    private String createAnomalyEventJson() throws Exception {
        AnomalyEvent event = new AnomalyEvent(
            UUID.randomUUID(),
            "PRECOUNT_DIFFERENCE",
            "HIGH",
            "76-001-01-01-001",
            "76",
            "001",
            "001",
            "0001",
            "Partido Test",
            "101",
            "Juan",
            "Pérez",
            100,
            85,
            -15,
            "2024-10-29",
            1L,
            "Elecciones 2024",
            Instant.now()
        );
        return objectMapper.writeValueAsString(event);
    }
}
