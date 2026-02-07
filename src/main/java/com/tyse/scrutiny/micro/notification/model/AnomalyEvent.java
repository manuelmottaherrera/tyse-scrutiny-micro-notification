package com.tyse.scrutiny.micro.notification.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Event received from e14-anomaly-detected topic.
 */
public record AnomalyEvent(
    UUID anomalyId,
    String type,
    String severity,
    String divipolKey,
    String depCode,
    String munCode,
    String table,
    String partyNumber,
    String partyName,
    String candidateId,
    String candidateFirstName,
    String candidateLastName,
    Integer precountVotes,
    Integer scrutinyVotes,
    Integer difference,
    String scrutinyDate,
    Long electionProcessId,
    String electionProcessName,
    Instant detectedAt
) {}
