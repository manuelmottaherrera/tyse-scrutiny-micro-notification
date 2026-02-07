package com.tyse.scrutiny.micro.notification.channel;

import com.tyse.scrutiny.micro.notification.model.NotificationChannel;
import com.tyse.scrutiny.micro.notification.service.TemplateService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationChannelTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateService templateService;

    @Mock
    private MimeMessage mimeMessage;

    private EmailNotificationChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmailNotificationChannel(mailSender, templateService);
        ReflectionTestUtils.setField(channel, "fromAddress", "test@tyse-scrutiny.com");
    }

    @Test
    void getChannelType_shouldReturnEmail() {
        assertThat(channel.getChannelType()).isEqualTo(NotificationChannel.EMAIL);
    }

    @Test
    void isEnabled_shouldReturnTrue() {
        assertThat(channel.isEnabled()).isTrue();
    }

    @Test
    void send_shouldProcessTemplateAndSendEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.processTemplate(anyString(), anyMap(), anyString()))
            .thenReturn("<html><body>Test content</body></html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        channel.send(
            "recipient@test.com",
            "activationEmail",
            Map.of("user", "test"),
            "Test Subject",
            "es"
        ).block();

        // Then
        verify(templateService).processTemplate(eq("activationEmail"), anyMap(), eq("es"));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void send_withNullSubject_shouldUseDefaultSubject() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateService.processTemplate(anyString(), anyMap(), anyString()))
            .thenReturn("<html>Content</html>");
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        channel.send(
            "recipient@test.com",
            "genericNotification",
            Map.of(),
            null,  // null subject
            "es"
        ).block();

        // Then
        verify(mailSender).send(mimeMessage);
    }
}
