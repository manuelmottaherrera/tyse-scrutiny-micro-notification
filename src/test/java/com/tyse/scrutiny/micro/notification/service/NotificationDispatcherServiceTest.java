package com.tyse.scrutiny.micro.notification.service;

import com.tyse.scrutiny.micro.notification.channel.EmailNotificationChannel;
import com.tyse.scrutiny.micro.notification.channel.NotificationChannelInterface;
import com.tyse.scrutiny.micro.notification.channel.SmsNotificationChannel;
import com.tyse.scrutiny.micro.notification.channel.WhatsAppNotificationChannel;
import com.tyse.scrutiny.micro.notification.config.NotificationProperties;
import com.tyse.scrutiny.micro.notification.model.AnomalyEvent;
import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.model.NotificationRequest;
import com.tyse.scrutiny.micro.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDispatcherServiceTest {

    @Mock
    private EmailNotificationChannel emailChannel;

    @Mock
    private WhatsAppNotificationChannel whatsAppChannel;

    @Mock
    private SmsNotificationChannel smsChannel;

    private NotificationDispatcherService service;
    private NotificationProperties properties;

    @BeforeEach
    void setUp() {
        // Configure properties
        properties = new NotificationProperties();
        properties.getAnomalyAlert().setDefaultRecipients(List.of("abogado@test.com"));
        properties.getAnomalyAlert().setMinimumSeverity("MEDIUM");
        properties.setBaseUrl("http://localhost:8080");

        List<NotificationChannelInterface> channels = List.of(emailChannel, whatsAppChannel, smsChannel);
        service = new NotificationDispatcherService(channels, properties);

        // Configure channel types
        when(emailChannel.getChannelType()).thenReturn(NotificationChannel.EMAIL);
        when(whatsAppChannel.getChannelType()).thenReturn(NotificationChannel.WHATSAPP);
        when(smsChannel.getChannelType()).thenReturn(NotificationChannel.SMS);

        when(emailChannel.isEnabled()).thenReturn(true);
        when(whatsAppChannel.isEnabled()).thenReturn(false);
        when(smsChannel.isEnabled()).thenReturn(false);
    }

    @Test
    void sendAnomalyAlert_withHighSeverity_shouldSendNotification() {
        // Given
        AnomalyEvent event = createAnomalyEvent("HIGH");
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendAnomalyAlert(event).block();

        // Then
        verify(emailChannel).send(
            eq("abogado@test.com"),
            eq("anomalyAlert"),
            anyMap(),
            contains("HIGH"),
            eq("es")
        );
    }

    @Test
    void sendAnomalyAlert_withLowSeverity_shouldSkipNotification() {
        // Given
        AnomalyEvent event = createAnomalyEvent("LOW");

        // When
        service.sendAnomalyAlert(event).block();

        // Then
        verify(emailChannel, never()).send(anyString(), anyString(), anyMap(), anyString(), anyString());
    }

    @Test
    void sendAnomalyAlert_withCriticalSeverity_shouldSendNotification() {
        // Given
        AnomalyEvent event = createAnomalyEvent("CRITICAL");
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendAnomalyAlert(event).block();

        // Then
        verify(emailChannel).send(anyString(), anyString(), anyMap(), anyString(), anyString());
    }

    @Test
    void sendNotification_accountActivation_shouldUseActivationTemplate() {
        // Given
        NotificationRequest request = new NotificationRequest(
            NotificationType.ACCOUNT_ACTIVATION,
            NotificationChannel.EMAIL,
            "user@test.com",
            Map.of("user", Map.of("firstName", "Test")),
            "es",
            Instant.now()
        );
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendNotification(request).block();

        // Then
        verify(emailChannel).send(
            eq("user@test.com"),
            eq("activationEmail"),
            anyMap(),
            eq("Activación de cuenta - Tyse Scrutiny"),
            eq("es")
        );
    }

    @Test
    void sendNotification_passwordReset_shouldUseResetTemplate() {
        // Given
        NotificationRequest request = new NotificationRequest(
            NotificationType.PASSWORD_RESET,
            NotificationChannel.EMAIL,
            "user@test.com",
            Map.of(),
            "es",
            Instant.now()
        );
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendNotification(request).block();

        // Then
        verify(emailChannel).send(
            eq("user@test.com"),
            eq("passwordResetEmail"),
            anyMap(),
            eq("Restablecer contraseña - Tyse Scrutiny"),
            eq("es")
        );
    }

    @Test
    void sendNotification_userCreation_shouldUseCreationTemplate() {
        // Given
        NotificationRequest request = new NotificationRequest(
            NotificationType.USER_CREATION,
            NotificationChannel.EMAIL,
            "user@test.com",
            Map.of(),
            "es",
            Instant.now()
        );
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendNotification(request).block();

        // Then
        verify(emailChannel).send(
            anyString(),
            eq("creationEmail"),
            anyMap(),
            anyString(),
            anyString()
        );
    }

    @Test
    void sendNotification_allChannels_shouldSendToAllEnabled() {
        // Given
        when(whatsAppChannel.isEnabled()).thenReturn(true);
        when(smsChannel.isEnabled()).thenReturn(true);

        NotificationRequest request = new NotificationRequest(
            NotificationType.ANOMALY_ALERT,
            NotificationChannel.ALL,
            "user@test.com",
            Map.of(),
            "es",
            Instant.now()
        );
        when(emailChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());
        when(whatsAppChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());
        when(smsChannel.send(anyString(), anyString(), anyMap(), anyString(), anyString()))
            .thenReturn(Mono.empty());

        // When
        service.sendNotification(request).block();

        // Then
        verify(emailChannel).send(anyString(), anyString(), anyMap(), anyString(), anyString());
        verify(whatsAppChannel).send(anyString(), anyString(), anyMap(), anyString(), anyString());
        verify(smsChannel).send(anyString(), anyString(), anyMap(), anyString(), anyString());
    }

    private AnomalyEvent createAnomalyEvent(String severity) {
        return new AnomalyEvent(
            UUID.randomUUID(),
            "PRECOUNT_DIFFERENCE",
            severity,
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
    }
}
