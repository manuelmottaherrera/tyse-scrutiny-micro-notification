package com.tyse.scrutiny.micro.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateServiceTest {

    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        templateService = new TemplateService(templateEngine);
    }

    @Test
    void processTemplate_anomalyAlert_shouldRenderHtml() {
        // Given
        Map<String, Object> data = new HashMap<>();
        data.put("severity", "HIGH");
        data.put("type", "PRECOUNT_DIFFERENCE");
        data.put("divipolKey", "76-001-01-01-001");
        data.put("table", "001");
        data.put("precountVotes", 100);
        data.put("scrutinyVotes", 85);
        data.put("difference", -15);
        data.put("anomalyId", "test-uuid");
        data.put("baseUrl", "http://localhost:8080");
        data.put("anomalyUrl", "http://localhost:8080/anomalies/test-uuid");
        data.put("electionProcessName", "Elecciones 2024");

        // When
        String result = templateService.processTemplate("anomalyAlert", data, "es");

        // Then
        assertThat(result).contains("Anomalía Detectada");
        assertThat(result).contains("HIGH");
        assertThat(result).contains("PRECOUNT_DIFFERENCE");
        assertThat(result).contains("76-001-01-01-001");
    }

    @Test
    void processTemplate_activationEmail_shouldRenderHtml() {
        // Given
        Map<String, Object> data = Map.of(
            "user", Map.of("firstName", "Juan"),
            "activationUrl", "http://localhost:8080/activate?key=abc123"
        );

        // When
        String result = templateService.processTemplate("activationEmail", data, "es");

        // Then
        assertThat(result).contains("Activar");
        assertThat(result).contains("Juan");
        assertThat(result).contains("activate?key=abc123");
    }

    @Test
    void processTemplate_passwordResetEmail_shouldRenderHtml() {
        // Given
        Map<String, Object> data = Map.of(
            "user", Map.of("firstName", "María"),
            "resetUrl", "http://localhost:8080/reset/finish?key=xyz789"
        );

        // When
        String result = templateService.processTemplate("passwordResetEmail", data, "es");

        // Then
        assertThat(result).contains("Restablecer");
        assertThat(result).contains("María");
        assertThat(result).contains("reset/finish?key=xyz789");
    }

    @Test
    void processTemplate_creationEmail_shouldRenderHtml() {
        // Given
        Map<String, Object> data = Map.of(
            "user", Map.of(
                "firstName", "Pedro",
                "login", "pedro123",
                "email", "pedro@test.com"
            ),
            "activationUrl", "http://localhost:8080/activate?key=new123"
        );

        // When
        String result = templateService.processTemplate("creationEmail", data, "es");

        // Then
        assertThat(result).contains("Cuenta Creada");
        assertThat(result).contains("Pedro");
        assertThat(result).contains("pedro123");
    }

    @Test
    void processTemplate_withNullLocale_shouldUseSpanish() {
        // Given
        Map<String, Object> data = Map.of(
            "title", "Test",
            "message", "Test message"
        );

        // When
        String result = templateService.processTemplate("genericNotification", data, null);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).contains("Test message");
    }

    @Test
    void processTemplate_invalidTemplate_shouldThrowException() {
        // Given
        Map<String, Object> data = Map.of();

        // When/Then
        assertThatThrownBy(() -> templateService.processTemplate("nonExistentTemplate", data, "es"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to process template");
    }
}
