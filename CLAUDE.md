# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## RULES

Responder en español

## Project Overview

**tyse-scrutiny-micro-notification** es el microservicio de notificaciones del sistema Tyse Scrutiny. Centraliza el envío de todas las notificaciones (email, WhatsApp, SMS) para:

1. **Alertas de anomalías** - Notifica a abogados cuando se detectan discrepancias entre preconteo y escrutinio
2. **Notificaciones de usuario** - Activación de cuentas, reset de contraseña, creación de usuarios (migrado desde el gateway)

**Stack tecnológico:**
- Spring Boot 3.4.5 con WebFlux
- Spring Cloud Stream + Kafka
- Consul para service discovery
- Thymeleaf para templates de email
- JavaMailSender para SMTP
- Puerto: 8085

## Development Commands

### Running the Application

```bash
# Iniciar la aplicación
./mvnw spring-boot:run

# O con Maven wrapper
mvn spring-boot:run
```

**Requisitos**:
- Kafka corriendo en localhost:9092
- Consul corriendo en localhost:8500
- MailHog (o SMTP) en localhost:1025
- Todos disponibles en tyse-scrutiny-infrastructure

### Building

```bash
./mvnw clean package
```

## Kafka Topics

**Consume**:
- `e14-anomaly-detected` - Eventos de anomalías detectadas por micro-scrutiny
- `notification-request` - Solicitudes genéricas de notificación (desde gateway)

## Canales de Notificación

1. **EmailNotificationChannel** - ✅ Implementado (JavaMailSender + Thymeleaf)
2. **WhatsAppNotificationChannel** - 🚧 Placeholder (requiere WhatsApp Business API)
3. **SmsNotificationChannel** - 🚧 Placeholder (requiere Twilio/AWS SNS)

## Templates de Email

```
src/main/resources/templates/mail/
├── anomalyAlert.html      # Alerta de anomalía electoral
├── activationEmail.html   # Activación de cuenta
├── passwordResetEmail.html # Reset de contraseña
├── creationEmail.html     # Cuenta creada por admin
└── genericNotification.html # Notificación genérica
```

## API Endpoints

```bash
# Enviar notificación de prueba
curl -X POST "http://localhost:8085/api/notifications/test?recipient=test@email.com"

# Health check
curl http://localhost:8085/api/notifications/health
```

## Configuración

### Severidad mínima para alertas
En `application.yml`:
```yaml
notification:
  anomaly-alert:
    minimum-severity: MEDIUM  # LOW, MEDIUM, HIGH, CRITICAL
    default-recipients:
      - abogado1@example.com
```

### MailHog para desarrollo
Los emails en desarrollo se capturan en MailHog:
- SMTP: localhost:1025
- Web UI: http://localhost:8025

## Estructura del Proyecto

```
src/main/java/com/tyse/scrutiny/micro/notification/
├── TyseScrutinyMicroNotificationApp.java
├── config/
├── consumer/
│   ├── AnomalyNotificationConsumer.java   # Escucha e14-anomaly-detected
│   └── NotificationRequestConsumer.java   # Escucha notification-request
├── channel/
│   ├── NotificationChannelInterface.java
│   ├── EmailNotificationChannel.java
│   ├── WhatsAppNotificationChannel.java
│   └── SmsNotificationChannel.java
├── service/
│   ├── NotificationDispatcherService.java
│   └── TemplateService.java
├── model/
│   ├── AnomalyEvent.java
│   ├── NotificationRequest.java
│   ├── NotificationType.java
│   └── NotificationChannel.java
└── web/
    └── NotificationResource.java
```

## Notas Importantes

1. **Stateless** - No tiene base de datos propia
2. **Templates Thymeleaf** - Los emails usan Thymeleaf con CSS inline
3. **Severidad configurable** - Solo envía alertas >= minimum-severity
4. **MailHog en dev** - Usa MailHog de tyse-scrutiny-infrastructure en desarrollo
